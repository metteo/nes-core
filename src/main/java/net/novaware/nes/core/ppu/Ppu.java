package net.novaware.nes.core.ppu;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.port.internal.DisplayPortImpl;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.DisplayMemory;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.ppu.table.AttributeTable;
import net.novaware.nes.core.ppu.table.NameTable;
import net.novaware.nes.core.ppu.table.PatternTable;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.signal.Signal.HIGH;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.ppu.inject.PpuVarName.AT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.NT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT1;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RST;
import static net.novaware.nes.core.ppu.inject.PpuVarName.S0H;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VBI;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * TODO: Stub PPU features: https://forums.nesdev.org/viewtopic.php?p=300322#p300322
 * TODO: create a separate stub ppu class that can be switched with real one?
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

    // FIXME: ppu should have the buffer directly here but in a separate object
    // PPU uses a method to write a pixel to back buffer but doesn't know which is it A or B.
    // when VBlank starts it calls swap method which also triggers the rest of the rendering pipeline for now the front buffer
    // TODO: also there is a single pixel buffer which delays pixel output to display memory

    // TODO: PPU doesn't hold state of the whole screen. it just outputs dots.
    //  renderer / virtual display should convert them to rgb/ntsc and assemble into frames
    private final DisplayMemory displayMemory;
    private final DisplayPortImpl displayPort;

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
        @PpuVar(DM) DisplayMemory displayMemory,
        DisplayPortImpl displayPort
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

        // will be swapped at the end of frame
        this.displayPort = displayPort;
        this.displayMemory = displayMemory;
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

        if (regs.dotCounter.getValue() == videoStandard.getPhysicalWidth()) {
            regs.dotCounter.reset();
            regs.scanLineCounter.increment();
        }

        // fake sprite zero hit
        if (regs.scanLineCounter.getValue() == 20 && regs.dotCounter.getValue() == 5) {
            if (regs.renderSprite.get()) {
                regs.status.setSpriteZeroHit(true);
                sprite0Hit.set(LOW);
            }
        }

        // last dot of last scan line
        if (regs.scanLineCounter.getValue() == videoStandard.getActiveHeight() - 1 && regs.dotCounter.getValue() == videoStandard.getActiveWidth() - 1) {
            // TODO: render the frame at once
            renderFrameAtOncePrototype();
        }

        // TODO: check post render scan line vs nmi trigger line
        if (regs.scanLineCounter.getValue() == VideoStandard.NTSC.getVerticalBlankStart() && (regs.dotCounter.getValue() == 1)) {
            regs.status.setVerticalBlank(true);
            if (regs.vBlankInterruptEnabled.get()) {
                vBlankInterrupt.set(LOW); // TODO: only set to low when vblank irq enabled and within vblank. reading status clears vblank flag so level goes high before end of vblank
            }

            displayPort.setDisplayBuffer(displayMemory);
            displayPort.onFrame(); // TODO: this occupies MasterClock thread so should be just a poke to rendering thread
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

    private void renderFrameAtOncePrototype() {
        for(int y = 0; y < videoStandard.getActiveHeight(); y++) {
            for(int x = 0; x < videoStandard.getActiveWidth(); x++) {
                if(x % 8 == 0) {
                    // nt address
                    bus.access(regs.currentViewPort.get()); // TODO: calculate the address
                }
                if (x % 8 == 1) {
                    // nt data read
                    @Unsigned byte nt = bus.read().data();
                }
                if (x % 8 == 2) {
                    // at address
                    bus.access(regs.currentViewPort.get()); // TODO: calculate the address
                }
                if (x % 8 == 3) {
                    // at data read
                    @Unsigned byte at = bus.read().data();
                }

                if (x % 8 == 4) {
                    // pt low address
                }

                if (x % 8 == 5) {
                    // pt low data read
                }

                if (x % 8 == 6) {
                    // pt hi address
                }

                if (x % 8 == 7) {
                    // pt hi data read
                }


                displayMemory.setColor(y, x, ubyte(1));
            }
        }
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
