package net.novaware.nes.core.ppu;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.ppu.register.VideoOutRegister;
import net.novaware.nes.core.ppu.table.AttributeTable;
import net.novaware.nes.core.ppu.table.NameTable;
import net.novaware.nes.core.ppu.table.PatternTable;
import net.novaware.nes.core.ppu.unit.ControlUnit;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;

import static net.novaware.nes.core.ppu.inject.PpuVarName.AT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.NT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT1;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RST;
import static net.novaware.nes.core.ppu.inject.PpuVarName.S0H;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VBI;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.USHORT_0;

/**
 * Picture Processing Unit
 *
 * 2C02 / 2C07 (NTSC / PAL)
 *
 * @see <a href="https://www.nesdev.org/wiki/PPU">PPU on nesdev.org</a>
 */
@BoardScope
public class Ppu implements ClockReceiver {

    private final VideoStandard videoStandard = VideoStandard.NTSC;

    private final MemoryBus bus;

    @Used  private final Pin vBlankInterrupt;
    @Used  private final Pin sprite0Hit;
    @Owned private final Pin rstPin;
    @Owned private final BooleanRegister rstReg;

    private final PpuRegFile regs;

    private final NameTable nameTable0;
    private final AttributeTable attributeTable0;

    private final PatternTable patternMemory0;
    private final PatternTable patternMemory1;

    private final PaletteMemory paletteMemory;
    private final ObjAttrMemory objAttrMemory;

    private final VideoOutRegister videoOut;
    private final ControlUnit controlUnit;

    @Inject
    public Ppu(
        @PpuVar(BUS) MemoryBus bus,
        @PpuVar(VBI) Pin vBlankInterrupt,
        @PpuVar(S0H) Pin sprite0Hit,
        @PpuVar(RST) Pin rstPin,
        @PpuVar(RST) BooleanRegister rstReg,
        PpuRegFile regs,
        @PpuVar(NT0) NameTable nameTable0,
        @PpuVar(AT0) AttributeTable attributeTable0,
        @PpuVar(PT0) PatternTable patternTable0,
        @PpuVar(PT1) PatternTable patternTable1,
        PaletteMemory paletteMemory,
        @PpuVar(OAM) ObjAttrMemory objAttrMemory,
        VideoOutRegister videoOut,
        ControlUnit controlUnit
    ) {
        this.bus = bus;
        this.vBlankInterrupt = vBlankInterrupt;
        this.sprite0Hit = sprite0Hit;
        this.rstPin = rstPin;
        this.rstReg = rstReg;
        this.regs = regs;
        this.nameTable0 = nameTable0;
        this.attributeTable0 = attributeTable0;
        this.patternMemory0 = patternTable0;
        this.patternMemory1 = patternTable1;
        this.paletteMemory = paletteMemory;
        this.objAttrMemory = objAttrMemory;

        this.videoOut = videoOut;
        this.controlUnit = controlUnit;
    }

    public void initialize() {
        regs.resetControl();
        regs.resetMask();

        regs.status.setVerticalBlank(true); // often set
        regs.status.setSpriteZeroHit(false); // 0
        regs.status.setSpriteOverflow(true); // often set

        regs.oamAddress.set(UBYTE_0);
        regs.secondWrite.set(false);

        regs.tempViewPort.set(USHORT_0);
        regs.currentViewPort.set(USHORT_0);
        regs.dataReadBuffer.set(UBYTE_0);

        regs.oddFrame.set(false);
        regs.resetLock.set(true);

        regs.cycleCounter.reset();
    }

    /**
     * Perform the reset of the PPU
     */
    /* package */ int reset() {
        regs.resetControl();
        regs.resetMask();

        regs.secondWrite.set(false);

        regs.tempViewPort.set(USHORT_0);
        regs.currentViewPort.set(USHORT_0);
        regs.dataReadBuffer.set(UBYTE_0);

        regs.oddFrame.set(false);
        regs.resetLock.set(true);

        // TODO: read on what ppu does during these first 21 cycles (nothing, warmup?)
        final int cycles = 7 * 3; // 21 dots = 7 cpu reset cycle times 3 (for NTSC only for now)

        regs.cycleCounter.setValue(cycles);
        regs.dotCounter.setValue(cycles);
        regs.scanLineCounter.reset();

        return cycles;
    }

    @Override
    public int cycle() {
        if (rstReg.get()) {
            return reset(); // TODO: reset signal continues for the first frame worth of cycles until very first vbl
        }

        int spent = controlUnit.cycle();

        regs.status.cycle();
        regs.renderBackground.cycle();
        regs.renderSprite.cycle();

        return spent;
    }

    public void reset(Signal s) {
        rstPin.set(s);
    }

    /**
     * ___
     * RST active-low line
     */
    public void rst(Signal s) { // NOTE: alias, maybe move to an interface as default method
        reset(s);
    }

    // region ext pins
    // TODO: expose EXT pins
    // endregion


}
