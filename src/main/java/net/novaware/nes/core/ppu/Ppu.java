package net.novaware.nes.core.ppu;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.DisplayMemory;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;

import static net.novaware.nes.core.cpu.signal.Signal.HIGH;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DBM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RST;
import static net.novaware.nes.core.ppu.inject.PpuVarName.S0H;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VBI;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.USHORT_0;

/**
 * TODO: Stub PPU features: https://forums.nesdev.org/viewtopic.php?p=300322#p300322
 * TODO: create a separate stub ppu class that can be switched with real one?
 */
@BoardScope
public class Ppu implements ClockReceiver {

    private final MemoryBus bus;

    @Used  private final Pin vBlankInterrupt;
    @Used  private final Pin sprite0Hit;
    @Owned private final Pin rstPin;
    @Owned private final BooleanRegister rstReg;

    private final PpuRegFile regs;

    private final PaletteMemory paletteMemory;
    private final ObjAttrMemory objAttrMemory;

    private final DisplayMemory frontBuffer;
    private final DisplayMemory backBuffer;

    @Inject
    public Ppu(
        @PpuVar(BUS) MemoryBus bus,
        @PpuVar(VBI) Pin vBlankInterrupt,
        @PpuVar(S0H) Pin sprite0Hit,
        @PpuVar(RST) Pin rstPin,
        @PpuVar(RST) BooleanRegister rstReg,
        PpuRegFile regs,
        PaletteMemory paletteMemory,
        ObjAttrMemory objAttrMemory,
        @PpuVar(DAM) DisplayMemory displayA,
        @PpuVar(DBM) DisplayMemory displayB
    ) {
        this.bus = bus;
        this.vBlankInterrupt = vBlankInterrupt;
        this.sprite0Hit = sprite0Hit;
        this.rstPin = rstPin;
        this.rstReg = rstReg;
        this.regs = regs;
        this.paletteMemory = paletteMemory;
        this.objAttrMemory = objAttrMemory;

        // will be swapped at the end of frame
        this.frontBuffer = displayA;
        this.backBuffer = displayB;
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
        regs.resetLock.set(true);
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
        regs.resetLock.set(true);

        // TODO: read on what ppu does during these first 21 cycles
        regs.dotCounter.setValue(7 * 3); // 21 dots = 7 cpu reset cycle times 3 (for NTSC only for now)
        regs.scanLineCounter.reset();
    }

    public int cycle0() {
        if (rstReg.get()) {
            reset();
            return 0;
        }

        regs.cycleCounter.increment();
        regs.dotCounter.increment();

        // TODO: on PAL there is no rendering on 0th scan line

        if (regs.dotCounter.getValue() == VideoStandard.NTSC.getPhysicalWidth()) {
            regs.dotCounter.reset();
            regs.scanLineCounter.increment();
        }

        if (regs.scanLineCounter.getValue() == 20 && regs.dotCounter.getValue() == 5) {
            if (regs.renderSprite.get()) {
                regs.status.setSpriteZeroHit(true);
                sprite0Hit.set(LOW);
            }
        }

        // TODO: check post render scan line vs nmi trigger line
        if (regs.scanLineCounter.getValue() == VideoStandard.NTSC.getVerticalBlankStart() && (regs.dotCounter.getValue() == 1)) {
            regs.status.setVerticalBlank(true);
            if (regs.vBlankInterruptEnabled.get()) {
                vBlankInterrupt.set(LOW); // TODO: only set to low when vblank irq enabled and within vblank. reading status clears vblank flag so level goes high before end of vblank
            }
        }

        if (regs.scanLineCounter.getValue() == VideoStandard.NTSC.getPhysicalHeight() - 1) {
            regs.status.setVerticalBlank(false);
            if (regs.vBlankInterruptEnabled.get()) {
                vBlankInterrupt.set(HIGH);
            }

            if (regs.renderSprite.get()) {
                regs.status.setSpriteZeroHit(false);
                sprite0Hit.set(HIGH);
            }
        }

        if (regs.scanLineCounter.getValue() == VideoStandard.NTSC.getPhysicalHeight()) {
            regs.scanLineCounter.reset();

            regs.resetLock.set(false); // TODO: should happen on the first pre-render scanline
        }

        regs.status.cycle();

        return 1; // TODO: return 0 for skipped cycle?
    }


    @Override
    public int cycle() {
        return cycle0();
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
