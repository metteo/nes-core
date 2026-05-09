package net.novaware.nes.core.ppu;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.util.uml.Owned;

import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.USHORT_0;

@BoardScope
public class Ppu {

    private final MemoryBus bus;

    private final PpuRegFile regs;
    private final PaletteMemory paletteMemory;
    private final ObjAttrMemory objAttrMemory;

    @Owned
    private final LevelDetector rst = new LevelDetector("RST", LOW); // TODO: inject

    @Inject
    public Ppu(
        @PpuVar(BUS) MemoryBus bus,
        PpuRegFile regs,
        PaletteMemory paletteMemory,
        ObjAttrMemory objAttrMemory
    ) {
        this.bus = bus;
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
    }

    public void cycle() {
        if (rst.isActive()) {
            reset();
            return;
        }

        System.out.println("PPU dot");
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
