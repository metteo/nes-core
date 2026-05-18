package net.novaware.nes.core.ppu;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.internal.Detector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;

import static net.novaware.nes.core.cpu.signal.Signal.HIGH;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RST;
import static net.novaware.nes.core.ppu.inject.PpuVarName.S0H;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VBI;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.USHORT_0;

@BoardScope
public class Ppu implements ClockReceiver {

    private final MemoryBus bus;

    @Used  private final Pin vBlankInterrupt;
    @Used  private final Pin sprite0Hit;
    @Owned private final Detector rst;

    private final PpuRegFile regs;
    private final PaletteMemory paletteMemory;
    private final ObjAttrMemory objAttrMemory;

    @Inject
    public Ppu(
        @PpuVar(BUS) MemoryBus bus,
        @PpuVar(VBI) Pin vBlankInterrupt,
        @PpuVar(S0H) Pin sprite0Hit,
        @PpuVar(RST) LevelDetector rst,
        PpuRegFile regs,
        PaletteMemory paletteMemory,
        ObjAttrMemory objAttrMemory
    ) {
        this.bus = bus;
        this.vBlankInterrupt = vBlankInterrupt;
        this.sprite0Hit = sprite0Hit;
        this.rst = rst;
        this.regs = regs;
        this.paletteMemory = paletteMemory;
        this.objAttrMemory = objAttrMemory;
    }

    public void initialize() {
        regs.cycleCounter.reset();

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
    }

    /**
     * Perform the reset of the PPU
     */
    /* package */ void reset() {
        regs.cycleCounter.reset();

        regs.resetControl();
        regs.resetMask();

        regs.secondWrite.set(false);

        regs.tempViewPort.set(USHORT_0);
        regs.currentViewPort.set(USHORT_0);
        regs.dataReadBuffer.set(UBYTE_0);

        regs.oddFrame.set(false);

        // TODO: read on what ppu does during these first 21 cycles
        regs.dotCounter.setValue(7 * 3); // 21 dots = 7 cpu reset cycle times 3 (for NTSC only for now)
        regs.scanLineCounter.reset();
    }

    @Override
    public int cycle() {
        if (rst.isActive()) {
            reset();
            return 0;
        }

        regs.cycleCounter.increment();
        regs.dotCounter.increment();

        if (regs.dotCounter.getValue() == VideoStandard.NTSC.getPhysicalWidth()) {
            regs.dotCounter.reset();
            regs.scanLineCounter.increment();
        }

        // TODO: check post render scanline vs nmi trigger line
        if (regs.scanLineCounter.getValue() == VideoStandard.NTSC.getVerticalBlankStart() && (regs.dotCounter.getValue() == 1 || regs.dotCounter.getValue() == 2 || regs.dotCounter.getValue() == 3)) {
            regs.status.setVerticalBlank(true);
            vBlankInterrupt.set(LOW);
        }

        // TODO: pre render scan line should be -1 or 261?
        if (regs.scanLineCounter.getValue() == VideoStandard.NTSC.getPhysicalHeight() - 1 && regs.dotCounter.getValue() == 1) {
            regs.status.setVerticalBlank(false);
            vBlankInterrupt.set(HIGH);
        }

        if (regs.scanLineCounter.getValue() == VideoStandard.NTSC.getPhysicalHeight()) {
            regs.scanLineCounter.reset();
        }

        return 1; // TODO: return 0 for skipped cycle?
    }

    public void reset(Signal s) {
        rst.set(s);
    }

    /**
     * ___
     * RST active-low line
     */
    void rst(Signal s) { // NOTE: alias, maybe move to an interface as default method
        reset(s);
    }
}
