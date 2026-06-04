package net.novaware.nes.core.ppu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.ppu.action.Action;
import net.novaware.nes.core.ppu.action.ScanLine;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.register.PpuStatusRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.IntegerCounter;

import java.util.Arrays;

import static net.novaware.nes.core.cpu.signal.Signal.HIGH;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.ppu.action.Action.ACCESS_ATTR_TABLE_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.ACCESS_BG_HI_BITS_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.ACCESS_BG_LO_BITS_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.ACCESS_NAME_TABLE_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.ACCESS_SP_HI_BITS_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.ACCESS_SP_LO_BITS_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.CLR_HBLANK;
import static net.novaware.nes.core.ppu.action.Action.IGNORED_NAME_TABLE_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.IGNORED_NAME_TABLE_DATA;
import static net.novaware.nes.core.ppu.action.Action.INCREMENT_X;
import static net.novaware.nes.core.ppu.action.Action.INCREMENT_Y;
import static net.novaware.nes.core.ppu.action.Action.NO_OPERATION;
import static net.novaware.nes.core.ppu.action.Action.READ_ATTR_TABLE_DATA;
import static net.novaware.nes.core.ppu.action.Action.READ_BG_HI_BITS_DATA;
import static net.novaware.nes.core.ppu.action.Action.READ_BG_LO_BITS_DATA;
import static net.novaware.nes.core.ppu.action.Action.READ_NAME_TABLE_DATA;
import static net.novaware.nes.core.ppu.action.Action.READ_SP_HI_BITS_DATA;
import static net.novaware.nes.core.ppu.action.Action.READ_SP_LO_BITS_DATA;
import static net.novaware.nes.core.ppu.action.Action.SET_HBLANK;
import static net.novaware.nes.core.ppu.action.Action.TRANSFER_TX_TO_X;
import static net.novaware.nes.core.ppu.action.Action.TRANSFER_TY_TO_Y;
import static net.novaware.nes.core.ppu.action.Action.UNUSED_NAME_TABLE_ADDRESS;
import static net.novaware.nes.core.ppu.action.Action.UNUSED_NAME_TABLE_DATA;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CV;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.HB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OF;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.S0H;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VBI;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;

/**
 * @see gemini: micro-action log
 */
@BoardScope
public class ControlUnit {

    private final VideoStandard videoStandard;

    private final IntegerCounter cycleCounter;
    private final IntegerCounter scanLineCounter;
    private final IntegerCounter dotCounter;
    private final BooleanRegister oddFrame;

    private final PpuStatusRegister status;
    private final BooleanRegister hBlank;
    private final BooleanRegister vBlankInterruptEnabled;
    private final Pin vBlankInterrupt;

    private final BooleanRegister renderSprite;
    private final Pin sprite0Hit;

    private final ScanLine[] scanLines;

    // TODO: consider microcode generated using rules below that stays inside a file
    private final Action[] busActions;
    private final Action[] oamActions;
    private final Action[] drawActions;
    private final Action[] renderingViewActions;
    private final Action[] preRenderViewActions;

    private final ViewPortRegister currentViewPort;
    private final ViewPortRegister tempViewPort;

    @Inject
    public ControlUnit(
        CoreConfig config,
        @PpuVar(CC) IntegerCounter cycleCounter,
        @PpuVar(SC) IntegerCounter scanLineCounter,
        @PpuVar(DC) IntegerCounter dotCounter,
        @PpuVar(OF) BooleanRegister oddFrame,

        @PpuVar(PS) PpuStatusRegister status,
        @PpuVar(HB) BooleanRegister hBlank,
        @PpuVar(CV) BooleanRegister vBlankInterruptEnabled,
        @PpuVar(VBI) Pin vBlankInterrupt,

        @PpuVar(RS) BooleanRegister renderSprite,
        @PpuVar(S0H) Pin sprite0Hit,
        @PpuVar(VX) ViewPortRegister currentViewPort,
        @PpuVar(T)  ViewPortRegister tempViewPort
    ) {
        this.currentViewPort = currentViewPort;
        this.tempViewPort = tempViewPort;

        final VideoStandard vs = config.getVideoStandard();

        this.videoStandard = vs;
        this.cycleCounter = cycleCounter;
        this.scanLineCounter = scanLineCounter;
        this.dotCounter = dotCounter;
        this.oddFrame = oddFrame;
        this.status = status;
        this.hBlank = hBlank;
        this.vBlankInterruptEnabled = vBlankInterruptEnabled;
        this.vBlankInterrupt = vBlankInterrupt;
        this.renderSprite = renderSprite;
        this.sprite0Hit = sprite0Hit;

        scanLines = initScanLines(vs);
        busActions = initBusActions(vs);
        oamActions = initOamActions(vs);
        drawActions = initDrawActions(vs);
        renderingViewActions = initViewActions(vs, false);
        preRenderViewActions = initViewActions(vs, true);
    }

    private static ScanLine[] initScanLines(VideoStandard videoStandard) {
        int height = videoStandard.getPhysicalHeight();

        ScanLine[] scanLines = new ScanLine[height];
        scanLines[0] = ScanLine.RENDER_START;

        for(int y = 1; y <= 238; y++) {
            scanLines[y] = ScanLine.RENDERING;
        }

        scanLines[239] = ScanLine.RENDER_END;
        scanLines[240] = ScanLine.POST_RENDER;
        scanLines[241] = ScanLine.BLANK_START;

        for(int y = 242; y < height - 2; y++) {
            scanLines[y] = ScanLine.BLANKING;
        }
        scanLines[height - 2] = ScanLine.BLANK_END;
        scanLines[height - 1] = ScanLine.PRE_RENDER;

        return scanLines;
    }

    private static Action[] initBusActions(VideoStandard videoStandard) {
        Action[] busActions = new Action[videoStandard.getPhysicalWidth()];
        Arrays.fill(busActions, Action.NO_OPERATION);

        // tile 3 of current background
        busActions[0] = ACCESS_BG_LO_BITS_ADDRESS;

        for (int x = 1; x <= 256; x+=8) { // current background
            busActions[x    ] = ACCESS_NAME_TABLE_ADDRESS;
            busActions[x + 1] = READ_NAME_TABLE_DATA;

            busActions[x + 2] = ACCESS_ATTR_TABLE_ADDRESS;
            busActions[x + 3] = READ_ATTR_TABLE_DATA;

            busActions[x + 4] = ACCESS_BG_LO_BITS_ADDRESS;
            busActions[x + 5] = READ_BG_LO_BITS_DATA;
            busActions[x + 6] = ACCESS_BG_HI_BITS_ADDRESS;
            busActions[x + 7] = READ_BG_HI_BITS_DATA;
        }

        for (int x = 257; x <= 320; x+=8) { // next sprite (8 / 16 tiles)
            // TODO: unused and ignored should be replaced with SP when extended SOAM
            busActions[x    ] = UNUSED_NAME_TABLE_ADDRESS;
            busActions[x + 1] = UNUSED_NAME_TABLE_DATA;
            busActions[x + 2] = IGNORED_NAME_TABLE_ADDRESS;
            busActions[x + 3] = IGNORED_NAME_TABLE_DATA;

            busActions[x + 4] = ACCESS_SP_LO_BITS_ADDRESS;
            busActions[x + 5] = READ_SP_LO_BITS_DATA;
            busActions[x + 6] = ACCESS_SP_HI_BITS_ADDRESS;
            busActions[x + 7] = READ_SP_HI_BITS_DATA;
        }

        for (int x = 321; x <= 336; x+=8) { // next background (tiles 1 & 2)
            busActions[x    ] = ACCESS_NAME_TABLE_ADDRESS;
            busActions[x + 1] = READ_NAME_TABLE_DATA;

            busActions[x + 2] = ACCESS_ATTR_TABLE_ADDRESS;
            busActions[x + 3] = READ_ATTR_TABLE_DATA;

            busActions[x + 4] = ACCESS_BG_LO_BITS_ADDRESS;
            busActions[x + 5] = READ_BG_LO_BITS_DATA;
            busActions[x + 6] = ACCESS_BG_HI_BITS_ADDRESS;
            busActions[x + 7] = READ_BG_HI_BITS_DATA;
        }

        { // unused / ignored next background (tile 3)
            busActions[337] = UNUSED_NAME_TABLE_ADDRESS;
            busActions[338] = UNUSED_NAME_TABLE_DATA;
            busActions[339] = IGNORED_NAME_TABLE_ADDRESS;
            busActions[340] = IGNORED_NAME_TABLE_DATA;
        }

        return busActions;
    }

    private static Action[] initOamActions(VideoStandard videoStandard) {
        Action[] oamActions = new Action[videoStandard.getPhysicalWidth()];
        Arrays.fill(oamActions, Action.NO_OPERATION);

        for(int x = 1; x <= 64; x++) {
            oamActions[x] = Action.CLR_SECONDARY_OAM; // TODO: it's reading primary and setting sec = 0xFF
        }

        for(int x = 65; x <= 256; x++) {
            oamActions[x] = Action.EVAL_PRIMARY_OAM; // TODO this is a multi step process, split the action
        }

        return oamActions;
    }

    private static Action[] initDrawActions(VideoStandard videoStandard) {
        Action[] drawActions = new Action[videoStandard.getPhysicalWidth()];
        Arrays.fill(drawActions, Action.NO_OPERATION);

        for(int x = 1; x <= 256; x++) {
            drawActions[x] = Action.RENDER;
        }

        return drawActions;
    }

    private static Action[] initViewActions(VideoStandard videoStandard, boolean preRender) {
        Action[] renderingViewActions = new Action[videoStandard.getPhysicalWidth()];
        Arrays.fill(renderingViewActions, Action.NO_OPERATION);

        for(int x = 8; x < 256; x+=8) {
            renderingViewActions[x] = INCREMENT_X;
        }

        renderingViewActions[256] = INCREMENT_Y;
        renderingViewActions[257] = TRANSFER_TX_TO_X; // hblank

        // tile 1,2 of next scanline
        renderingViewActions[328] = INCREMENT_X;
        renderingViewActions[336] = INCREMENT_X;

        if (preRender) {
            for (int x = 280; x <= 304; x++) {
                renderingViewActions[x] = TRANSFER_TY_TO_Y;
            }
        }

        return renderingViewActions;
    }

    public int cycle() {
        cycleCounter.increment();

        final int scanLine = scanLineCounter.getValue();
        final int dot = dotCounter.getValue();

        Action bus = NO_OPERATION;
        Action view = NO_OPERATION;
        Action oam = NO_OPERATION;
        Action draw = NO_OPERATION;
        Action flag = NO_OPERATION;

        switch(scanLines[scanLine]) {
            case RENDER_START:
                bus = busActions[dot];
                view = renderingViewActions[dot];
                oam = oamActions[dot];
                draw = drawActions[dot];
                flag = getRenderStartFlagAction(dot);
                break;
            case RENDERING:
            case RENDER_END:
                bus = busActions[dot];
                view = renderingViewActions[dot];
                oam = oamActions[dot];
                draw = drawActions[dot];
                flag = getRenderingFlagAction(dot);
                break;
            case POST_RENDER:
                bus = getPostRenderBusAction(dot);
                flag = getPostRenderFlagAction(dot);
                break;
            case BLANK_START:
                flag = getVBlankStartFlagAction(dot);
                break;
            case BLANKING:
            case BLANK_END:
                // NOOP for all
                break;
            case PRE_RENDER:
                bus = busActions[dot];
                view = preRenderViewActions[dot];
                flag = getPreRenderFlagAction(dot);
                // TODO: resetLock.set(false);
                break;
        }

        // TODO: create a golden micro action file and compare in tests? for ntsc, pal, etc
        //System.out.println(dot + ": " + bus.getMnemonic() + " " + view.getMnemonic() + " " + oam.getMnemonic() + " " + draw.getMnemonic() + " " + flag.getMnemonic());

        switch(bus) {
            case ACCESS_NAME_TABLE_ADDRESS -> {}
            case READ_NAME_TABLE_DATA      -> {}
            case ACCESS_ATTR_TABLE_ADDRESS -> {}
            case READ_ATTR_TABLE_DATA      -> {}
            case ACCESS_BG_LO_BITS_ADDRESS -> {}
            case READ_BG_LO_BITS_DATA      -> {}
            case ACCESS_BG_HI_BITS_ADDRESS -> {}
            case READ_BG_HI_BITS_DATA      -> {}
            case UNUSED_NAME_TABLE_ADDRESS -> {}
            case UNUSED_NAME_TABLE_DATA    -> {}
            case IGNORED_NAME_TABLE_ADDRESS -> {}
            case IGNORED_NAME_TABLE_DATA   -> {}
            case ACCESS_SP_LO_BITS_ADDRESS -> {}
            case READ_SP_LO_BITS_DATA      -> {}
            case ACCESS_SP_HI_BITS_ADDRESS -> {}
            case READ_SP_HI_BITS_DATA      -> {}
            case NO_OPERATION              -> {}
            default -> throw new IllegalStateException("Unexpected bus action: " + bus);
        }

        // FIXME: force blank (rendering off) should not alter VX
        switch(view) {
            case INCREMENT_X -> currentViewPort.incrementX();
            case INCREMENT_Y -> currentViewPort.incrementY();
            case TRANSFER_TX_TO_X -> tempViewPort.transferX(currentViewPort);
            case TRANSFER_TY_TO_Y -> tempViewPort.transferY(currentViewPort);
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected view action: " + view);
        }

        switch(oam) {
            case CLR_SECONDARY_OAM -> {}
            case EVAL_PRIMARY_OAM -> {}
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected oam action: " + oam);
        }

        switch(draw) {
            case RENDER -> {}
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected draw action: " + draw);
        }

        switch(flag) {
            case SET_HBLANK -> hBlank.set(true);
            case CLR_HBLANK -> hBlank.set(false);
            case SET_VBLANK -> setVBlank(true);
            case CLR_STATUS -> clearStatus();
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected flag action: " + flag);
        }

        nextDot();

        return 1;
    }

    private Action getPostRenderBusAction(int dot) {
        return dot == 0 ? ACCESS_BG_LO_BITS_ADDRESS : NO_OPERATION;
    }

    private Action getPostRenderFlagAction(int dot) {
        return dot == 0 ? CLR_HBLANK : NO_OPERATION;
    }

    private void setVBlank(boolean verticalBlank) {
        status.setVerticalBlank(verticalBlank);
        if (vBlankInterruptEnabled.get()) {
            vBlankInterrupt.set(verticalBlank ? LOW : HIGH);
        }
    }

    private Action getRenderingFlagAction(int dot) {
        return switch(dot) {
            case 257 -> SET_HBLANK;
            case 0   -> CLR_HBLANK;
            default  -> NO_OPERATION;
        };
    }

    private Action getRenderStartFlagAction(int dot) {
        return dot == 257 ? SET_HBLANK : NO_OPERATION;
    }

    private Action getVBlankStartFlagAction(int dot) {
        return dot == 1 ? Action.SET_VBLANK : Action.NO_OPERATION;
    }

    private Action getPreRenderFlagAction(int dot) {
        return dot == 1 ? Action.CLR_STATUS : Action.NO_OPERATION;
    }

    private void clearStatus() {
        setVBlank(false);

        status.setSpriteZeroHit(false);
        if (renderSprite.get()) {
            sprite0Hit.set(HIGH);
        }
        status.setSpriteOverflow(false);
    }

    /* package */ void nextDot() {
        dotCounter.increment();

        if (dotCounter.getValue() == videoStandard.getPhysicalWidth()) {
            dotCounter.reset();
            scanLineCounter.increment();
        }

        if (scanLineCounter.getValue() == videoStandard.getPhysicalHeight()) {
            scanLineCounter.reset();

            // TODO: skip last dot of prerender, not first dot of RENDER_START
            boolean skipZeroZeroDotOnEvenFrame = videoStandard.isOddFrameCycleSkip() && oddFrame.get();
            dotCounter.maybeIncrement(skipZeroZeroDotOnEvenFrame);

            oddFrame.toggle();
        }
    }

    String printActions() {
        StringBuilder actions = new StringBuilder();

        for (int x = 0; x < busActions.length; x++) {
            actions
                .append(x).append(":")
                .append(busActions[x].name()).append("\t\t")
                .append(oamActions[x].name()).append("\t\t")
                .append(renderingViewActions[x].name()).append("\t\t")
                .append(drawActions[x].name()).append("\t\t")
                .append("\n");
        }

        return actions.toString();
    }

    String printScanLines() {
        StringBuilder scanLines = new StringBuilder();

        for (int y = 0; y < this.scanLines.length; y++) {
            scanLines
                .append(y).append(":")
                .append(this.scanLines[y].name()).append("\n");
        }

        return scanLines.toString();
    }
}
