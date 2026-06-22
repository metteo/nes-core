package net.novaware.nes.core.ppu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.ppu.action.Action;
import net.novaware.nes.core.ppu.action.ScanLine;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory.ObjAttrEntry;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory.Section;
import net.novaware.nes.core.ppu.memory.PpuBus;
import net.novaware.nes.core.ppu.register.PpuStatusRegister;
import net.novaware.nes.core.ppu.register.VideoOutRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.ppu.table.AttributeTable;
import net.novaware.nes.core.register.BooleanPipeline;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.register.ShortShifter;
import net.novaware.nes.core.util.Initializable;
import org.checkerframework.checker.signedness.qual.Unsigned;

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
import static net.novaware.nes.core.ppu.action.Action.SHIFT;
import static net.novaware.nes.core.ppu.action.Action.TRANSFER_TX_TO_X;
import static net.novaware.nes.core.ppu.action.Action.TRANSFER_TY_TO_Y;
import static net.novaware.nes.core.ppu.action.Action.UNUSED_NAME_TABLE_DATA;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CV;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.FT;
import static net.novaware.nes.core.ppu.inject.PpuVarName.HB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RL;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.S0H;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VBI;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;
import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.BACKGROUND;
import static net.novaware.nes.core.ppu.memory.PaletteMemory.Section.FOREGROUND;
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see gemini: micro-action log
 */
@BoardScope
public class ControlUnit implements Initializable {

    private final VideoStandard videoStandard;

    private final IntegerCounter cycleCounter;
    private final IntegerCounter lineCounter;
    private final IntegerCounter dotCounter;
    private final BooleanRegister frameToggle;

    private final PpuStatusRegister status;
    private final BooleanRegister hBlank;
    private final BooleanRegister vBlankInterruptEnabled;
    private final Pin vBlankInterrupt;

    private final BooleanPipeline renderSprite;
    private final BooleanPipeline renderBackground;
    private final Pin sprite0Hit;

    private final ScanLine[] scanLines;

    // TODO: consider microcode generated using rules below that stays inside a file
    private final Action[] shiftActions;
    private final Action[] busActions;
    private final Action[] oamActions;
    private final Action[] drawActions;
    private final Action[] renderingViewActions;
    private final Action[] preRenderViewActions;

    private final TimingUnit timingUnit;
    private final ViewPortRegister currentViewPort;
    private final ViewPortRegister tempViewPort;
    private final BooleanRegister resetLock;
    private final PpuBus bus;
    private final ShortRegister backgroundPatternTable;
    private final ShortRegister spritePatternTable;
    private final VideoOutRegister videoOut;
    private final PaletteMemory paletteMemory;
    private final ObjAttrMemory objAttrMemory;

    public ByteRegister nameTableBuffer = new ByteRegister("NT.BUF"); // tile xy

    public ShortRegister attributesBuffer = new ShortRegister("AT.BUF");
    public ShortRegister backgroundBuffer = new ShortRegister("BG.BUF");

    // TODO: have an array or sth that holds dot coords (x,y) so final video output is timed correctly, or just -1?
    public ShortShifter background = new ShortShifter("BG.SFT");
    public ShortShifter attributes = new ShortShifter("AT.SFT");

    public SpriteOutput[] spriteOutputUnits;

    @Inject
    public ControlUnit(
        CoreConfig config,
        TimingUnit timingUnit,
        @PpuVar(CC) IntegerCounter cycleCounter,
        @PpuVar(LC) IntegerCounter lineCounter,
        @PpuVar(DC) IntegerCounter dotCounter,
        @PpuVar(FT) BooleanRegister frameToggle,

        @PpuVar(PS) PpuStatusRegister status,
        @PpuVar(HB) BooleanRegister hBlank,
        @PpuVar(CV) BooleanRegister vBlankInterruptEnabled,
        @PpuVar(VBI) Pin vBlankInterrupt,

        @PpuVar(RS) BooleanPipeline renderSprite,
        @PpuVar(RB) BooleanPipeline renderBackground,
        @PpuVar(S0H) Pin sprite0Hit,
        @PpuVar(VX) ViewPortRegister currentViewPort,
        @PpuVar(T)  ViewPortRegister tempViewPort,

        @PpuVar(RL) BooleanRegister resetLock,
        PpuBus bus,
        @PpuVar(CB) ShortRegister backgroundPatternTable,
        @PpuVar(CS) ShortRegister spritePatternTable,
        VideoOutRegister videoOut,
        PaletteMemory paletteMemory,
        @PpuVar(OAM) ObjAttrMemory objAttrMemory
    ) {
        this.timingUnit = timingUnit;
        this.currentViewPort = currentViewPort;
        this.tempViewPort = tempViewPort;
        this.resetLock = resetLock;
        this.bus = bus;
        this.backgroundPatternTable = backgroundPatternTable;
        this.spritePatternTable = spritePatternTable;
        this.videoOut = videoOut;
        this.paletteMemory = paletteMemory;
        this.objAttrMemory = objAttrMemory;

        final VideoStandard vs = config.getVideoStandard();

        this.videoStandard = vs;
        this.cycleCounter = cycleCounter;
        this.lineCounter = lineCounter;
        this.dotCounter = dotCounter;
        this.frameToggle = frameToggle;
        this.status = status;
        this.hBlank = hBlank;
        this.vBlankInterruptEnabled = vBlankInterruptEnabled;
        this.vBlankInterrupt = vBlankInterrupt;
        this.renderSprite = renderSprite;
        this.renderBackground = renderBackground;
        this.sprite0Hit = sprite0Hit;

        scanLines = initScanLines(vs);
        shiftActions = initShiftActions(vs);
        busActions = initBusActions(vs);
        oamActions = initOamActions(vs);
        drawActions = initDrawActions(vs);
        renderingViewActions = initViewActions(vs, false);
        preRenderViewActions = initViewActions(vs, true);

        spriteOutputUnits = new SpriteOutput[objAttrMemory.getSecondarySize()];

        for(int i = 0; i < spriteOutputUnits.length; i++) {
            spriteOutputUnits[i] = new SpriteOutput();
        }
    }

    @Override
    public void initialize() {
        timingUnit.initialize();
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

    private static Action[] initShiftActions(VideoStandard videoStandard) {
        Action[] shiftActions = new Action[videoStandard.getPhysicalWidth()];
        Arrays.fill(shiftActions, Action.NO_OPERATION);

        for (int x = 1; x <= 256; x++) { // current background
            shiftActions[x] = SHIFT;
        }

        for (int x = 321; x <= 336; x++) { // next background (tiles 1 & 2)
            shiftActions[x] = SHIFT;
        }

        return shiftActions;
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
            busActions[x    ] = ACCESS_NAME_TABLE_ADDRESS;
            busActions[x + 1] = UNUSED_NAME_TABLE_DATA;
            busActions[x + 2] = ACCESS_NAME_TABLE_ADDRESS;
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
            busActions[337] = ACCESS_NAME_TABLE_ADDRESS;
            busActions[338] = UNUSED_NAME_TABLE_DATA;
            busActions[339] = ACCESS_NAME_TABLE_ADDRESS;
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

    public int cycle() { // TODO: PPU power up state lasts 1 full frame until first vBlank
                         //  only some registers can be used
        cycleCounter.increment();

        final int scanLine = lineCounter.getValue();
        final int dot = dotCounter.getValue();

        final boolean forceBlank = !(renderSprite.get() || renderBackground.get());

        Action draw = NO_OPERATION;
        Action shift = NO_OPERATION;
        Action bus = NO_OPERATION;
        Action view = NO_OPERATION;
        Action oam = NO_OPERATION;
        Action flag = NO_OPERATION;

        switch(scanLines[scanLine]) {
            case RENDER_START:
                draw = drawActions[dot];
                shift = shiftActions[dot];
                bus = busActions[dot];
                view = renderingViewActions[dot];
                oam = oamActions[dot];
                flag = getRenderStartFlagAction(dot);
                break;
            case RENDERING:
            case RENDER_END:
                draw = drawActions[dot];
                shift = shiftActions[dot];
                bus = busActions[dot];
                view = renderingViewActions[dot];
                oam = oamActions[dot];
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
                draw = getPreRenderDrawAction(dot);
                shift = shiftActions[dot];
                bus = busActions[dot];
                view = preRenderViewActions[dot];
                flag = getPreRenderFlagAction(dot);
                resetLock.set(false); // first pre-render
                break;
        }

        // TODO: create a golden micro action file and compare in tests? for ntsc, pal, etc
        //System.out.println(dot + ": " + bus.getMnemonic() + " " + view.getMnemonic() + " " + oam.getMnemonic() + " " + draw.getMnemonic() + " " + flag.getMnemonic());

        if (!resetLock.get() && !forceBlank) {
            executeDraw(draw);
            executeShift(shift);
//            executeShift(dot);
            executeBus(bus);
            executeView(view);
            executeOam(oam);
        }

        executeFlag(flag);
        timingUnit.increment();

        return 1;
    }

    private void executeShift(Action shiftAction) {
        switch(shiftAction) {
            case SHIFT -> shiftShiftRegisters();
            case NO_OPERATION -> {}
        }
    }

    private void executeShift(int dot) { // NOTE: temp experiment to see how branching is slower
        if (1 <= dot && dot <=256 || 321 <= dot && dot <= 336) {
            shiftShiftRegisters();
        }
    }

    private int secOamIndex = 0;

    private void executeBus(Action busAction) {
        switch(busAction) {
            case ACCESS_NAME_TABLE_ADDRESS -> {
                @Unsigned short nameTableAddr = currentViewPort.getNameTableAddress();
                bus.access(nameTableAddr);
            }
            case READ_NAME_TABLE_DATA      -> {
                @Unsigned byte nameTableData = bus.read().data();
                nameTableBuffer.set(nameTableData);
            }
            case ACCESS_ATTR_TABLE_ADDRESS -> {
                @Unsigned short attrTableAddr = currentViewPort.getAttrTableAddress();
                bus.access(attrTableAddr);
            }
            case READ_ATTR_TABLE_DATA      -> {
                @Unsigned byte attrTableData = bus.read().data();
                extractCurrentAttribute(attrTableData);
            }
            case ACCESS_BG_LO_BITS_ADDRESS -> {
                int bgLoAddr = getBackgroundPatternAddress(0);
                bus.access(ushort(bgLoAddr));
            }
            case READ_BG_LO_BITS_DATA      -> {
                @Unsigned byte bgLoData = bus.read().data();
                backgroundBuffer.low(bgLoData);
            }
            case ACCESS_BG_HI_BITS_ADDRESS -> {
                int bgHiAddr = getBackgroundPatternAddress(1);
                bus.access(ushort(bgHiAddr));
            }
            case READ_BG_HI_BITS_DATA      -> {
                @Unsigned byte bgHiData = bus.read().data();
                backgroundBuffer.high(bgHiData);
            }

            case UNUSED_NAME_TABLE_DATA    -> unusedNameTable(bus.read().data());
            case IGNORED_NAME_TABLE_DATA   -> ignoredNameTable(bus.read().data());

            case ACCESS_SP_LO_BITS_ADDRESS -> {

                ObjAttrEntry sprite = objAttrMemory.getSecondary(secOamIndex);
                SpriteOutput output = spriteOutputUnits[secOamIndex];

                // TODO: loading oam attrs & x is not instant, happens in garbage cycles
                output.hidden = sprite.hidden;
                output.palette = sprite.palette;
                output.countDown.setValue(sint(sprite.x));
                output.xCounter.setValue(7);
                // TODO: possibly to early, bg shifting already starts on the previous line
                output.state = SpriteOutput.State.WAITING;

                int spLoAddr = getSpritePatternAddress(sint(sprite.y), sint(sprite.tile), 0);

                bus.access(ushort(spLoAddr));
            }
            case READ_SP_LO_BITS_DATA      -> {
                ObjAttrEntry sprite = objAttrMemory.getSecondary(secOamIndex);
                @Unsigned byte spLoData = bus.read().data();

                if (sprite.flipH) {
                    spLoData = ubyte(Integer.reverse(sint(spLoData))>>24);
                }

                spriteOutputUnits[secOamIndex].shifter.loadPlaneLow(spLoData);
            }
            case ACCESS_SP_HI_BITS_ADDRESS -> {
                ObjAttrEntry sprite = objAttrMemory.getSecondary(secOamIndex);

                int spHiAddr = getSpritePatternAddress(sint(sprite.y), sint(sprite.tile), 1);
                bus.access(ushort(spHiAddr));
            }
            case READ_SP_HI_BITS_DATA      -> {
                ObjAttrEntry sprite = objAttrMemory.getSecondary(secOamIndex);
                @Unsigned byte spHiData = bus.read().data();

                if (sprite.flipH) {
                    spHiData = ubyte(Integer.reverse(sint(spHiData))>>24);
                }

                spriteOutputUnits[secOamIndex].shifter.loadPlaneHigh(spHiData);

                secOamIndex++;
            }
            case NO_OPERATION              -> {}
            default -> throw new IllegalStateException("Unexpected bus action: " + busAction);
        }
    }

    private void ignoredNameTable(@Unsigned byte data) {
        nameTableBuffer.set(data);
    }

    @SuppressWarnings("unused") // data parameter on purpose
    private void unusedNameTable(@Unsigned byte data) {}

    private void extractCurrentAttribute(@Unsigned byte attrTableData) {
        int attrBitsLatch = sint(AttributeTable.getSubAttribute(attrTableData, currentViewPort));
        int attrLoBitLatch = attrBitsLatch & 0b01;
        int attrHiBitLatch = (attrBitsLatch & 0b10) >> 1;

        attributesBuffer.low(ubyte(attrLoBitLatch * 0xFF));
        attributesBuffer.high(ubyte(attrHiBitLatch * 0xFF));
    }

    private void loadShifters() {
        background.loadPlaneHigh(backgroundBuffer.high());
        background.loadPlaneLow(backgroundBuffer.low());

        attributes.loadPlaneLow(attributesBuffer.low());
        attributes.loadPlaneHigh(attributesBuffer.high());
    }

    // TODO: move to pattern table?
    private int getBackgroundPatternAddress(int plane) {
        int half = backgroundPatternTable.getAsInt();
        int tile = nameTableBuffer.getAsInt() << 4;
        int planeShifted = plane << 3;
        int tileRow = currentViewPort.getFineY();

        int bgAddr = half | tile | planeShifted | tileRow;
        return bgAddr;
    }

    private int getSpritePatternAddress(int y, int tile, int plane) { // TODO: for 8x8 sprites only for now
        int half = spritePatternTable.getAsInt();
        int tileShifted = tile << 4;
        int planeShifted = plane << 3;
        int tileRow = Math.max(0, lineCounter.getValue() - y); // FIXME: max because eval doesn't check y, remove later

        int spAddr = half | tileShifted | planeShifted | tileRow;

        return spAddr;
    }

    private void executeFlag(Action flag) {
        switch(flag) {
            case SET_HBLANK -> hBlank.set(true);
            case CLR_HBLANK -> hBlank.set(false);
            case SET_VBLANK -> setVBlank(true);
            case CLR_STATUS -> clearStatus();
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected flag action: " + flag);
        }
    }

    private void executeDraw(Action draw) {
        switch(draw) {
            case RENDER -> {

                // TODO: mux pattern bits with attr bits using fine x
                selectBgAndAttrBits();

                // NOTE: shifting of sprites happens only during rendering
                for(int i = 0; i < spriteOutputUnits.length; i++) {
                    SpriteOutput spriteOutputUnit = spriteOutputUnits[i];
                    spriteOutputUnit.maybeShiftPlanes();
                }

                // TODO: push the dot to priority mux
                // TODO: push previous dot to EXT
                // TODO: read dot from EXT and MUX it with previous+2 dot
                // TODO: push previous+3 dot to videoOutRegister
            }
            case CLEAR -> {
                // TODO: on pal border region is always black
                @Unsigned byte backdrop = paletteMemory.getColor(BACKGROUND, 1, 1); // TODO: for debugging, should be 0, 0);
                videoOut.set(-1, -1, backdrop);
            }
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected draw action: " + draw);
        }
    }

    private void selectBgAndAttrBits() {
        final int fineX = currentViewPort.getFineX();

        // Backdrop
        Section section = BACKGROUND;
        int palette = 0;
        int offset = 0;
                                                         // Bits
        Section sectionBg = BACKGROUND;                  // 4
        int paletteBg = sint(attributes.getBits(fineX)); // 3-2
        int offsetBg  = sint(background.getBits(fineX)); // 1-0

        // SPRITES PRIORITY MUX

        Section sectionSp = FOREGROUND;
        int paletteSp = 0;
        int offsetSp = 0;
        boolean hiddenSp = false;

        for(int i = 0; i < spriteOutputUnits.length; i++) { // TODO: go backwards and the last non transparent pixel wins?
            SpriteOutput spriteOutput = spriteOutputUnits[i];

            if (spriteOutput.state == SpriteOutput.State.DRAWING) {
                int paletteSp2 = sint(spriteOutput.palette);
                int offsetSp2 = sint(spriteOutput.shifter.getBits(0));

                if (offsetSp == 0 && offsetSp2 != 0) {
                    paletteSp = paletteSp2;
                    offsetSp = offsetSp2;
                    hiddenSp = spriteOutput.hidden;
                }
            }
        }

        if (offsetBg == 0) {
            if (offsetSp != 0) {
                section = sectionSp;
                palette = paletteSp;
                offset = offsetSp;
            } // else backdrop (default)
        } else {
            if (offsetSp == 0) {
                section = sectionBg;
                palette = paletteBg;
                offset = offsetBg;
            } else {
                if (hiddenSp) {
                    section = sectionBg;
                    palette = paletteBg;
                    offset = offsetBg;
                } else {
                    section = sectionSp;
                    palette = paletteSp;
                    offset = offsetSp;
                }
            }
        }

        @Unsigned byte color = paletteMemory.getColor(section, palette, offset);

        // TODO: too early to output, do priority, ext in / out muxing
        videoOut.set(lineCounter.getValue(), dotCounter.getValue() - 1, color);
    }

    private void shiftShiftRegisters() {
        background.shiftPlanes();
        attributes.shiftPlanes();
    }

    private void executeOam(Action oam) {
        switch(oam) {
            case CLR_SECONDARY_OAM -> {
                // TODO: should be alternating between pri oam reads and sec oam write with 0xFF
                // TODO: use OAMADDR register as a base for reading primary oam
                int dot = dotCounter.getValue();
                int secOamAddr = dot / 2;
                objAttrMemory.writeSecondary(ubyte(secOamAddr), UBYTE_MAX_VALUE);
            }
            case EVAL_PRIMARY_OAM -> { // FIXME: just get first 8 sprites
                int dot = dotCounter.getValue();
                if (dot == 65) {
                    int secOamI = 0;
                    // TODO: create "VIEW" action that resets oamaddr and sec oam addr? or not
                    for(int i = 0; i < 0xFF; i+=4) { // TODO: use OAMADDR instead of i
                        int y = sint(objAttrMemory.readPrimary(ubyte(i)));
                        int futureY = lineCounter.getValue() + 1;
                        if (y < futureY && futureY <= y+8) {
                            objAttrMemory.copyToSecondary(i / 4, secOamI); // TODO: use SEC OAM addr to iterate over the bytes
                            secOamI++;

                            if (secOamI >= objAttrMemory.getSecondarySize()) {
                                // TODO: set sprite overflow?
                                break;
                            }
                        }
                    }
                }
            }
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected oam action: " + oam);
        }
    }

    private void executeView(Action view) {
        // FIXME: force blank (rendering off) should not alter VX
        switch(view) {
            case INCREMENT_X -> {
                loadShifters();
                currentViewPort.incrementX();
            }
            case INCREMENT_Y -> currentViewPort.incrementY();
            case TRANSFER_TX_TO_X -> {
                tempViewPort.transferX(currentViewPort);
                secOamIndex = 0;
            }
            case TRANSFER_TY_TO_Y -> tempViewPort.transferY(currentViewPort);
            case NO_OPERATION -> {}
            default -> throw new IllegalStateException("Unexpected view action: " + view);
        }
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

    private Action getPreRenderDrawAction(int dot) {
        return dot == 1 ? Action.CLEAR : NO_OPERATION;
    }

    private void clearStatus() {
        setVBlank(false);

        status.setSpriteZeroHit(false);
        if (renderSprite.get()) {
            sprite0Hit.set(HIGH);
        }
        status.setSpriteOverflow(false);
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
