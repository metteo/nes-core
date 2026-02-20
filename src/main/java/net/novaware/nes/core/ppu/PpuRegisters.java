package net.novaware.nes.core.ppu;

import net.novaware.nes.core.cpu.memory.MemoryMap;
import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.novaware.nes.core.util.UTypes.sint;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org
 */
public class PpuRegisters extends RegisterFile {

    public CycleCounter cycleCounter = new CycleCounter("PPUCC");

    // TODO: enforce R / W / RW / Wx2 specifics
    private ByteRegister ppuCtrl = new ByteRegister("PPUCTRL"); // 0x2000
    private ByteRegister ppuMask = new ByteRegister("PPUMASK"); // ...
    private ByteRegister ppuStatus = new ByteRegister("PPUSTATUS");

    private ByteRegister oamAddr = new ByteRegister("OAMADDR");
    private ByteRegister oamData = new ByteRegister("OAMDATA");

    private ByteRegister ppuScroll = new ByteRegister("PPUSCROLL");
    private ByteRegister ppuAddr = new ByteRegister("PPUADDR");
    private ByteRegister ppuData = new ByteRegister("PPUDATA"); // 0x2007

    // TODO: create special register for those that accepts 2 writes
    private ShortRegister ppuScrollFull = new ShortRegister("PPUSCROLL_FULL");
    private ShortRegister ppuAddrFull = new ShortRegister("PPUADDR_FULL");

    private @Nullable ByteRegister oamDma = null; // 0x4014

    // Internal
    private ByteRegister v = new ByteRegister("V");
    private ByteRegister t = new ByteRegister("T");
    private ByteRegister x = new ByteRegister("X");
    private ByteRegister w = new ByteRegister("W");

    public PpuRegisters() {
        super("PPU");

        dataRegisters = List.of(
                ppuCtrl, ppuMask, ppuStatus,
                oamAddr, oamData,
                ppuScroll, ppuAddr, ppuData,
                v, t, x, w
        );

        addressRegisters = List.of(ppuScrollFull, ppuAddrFull);
    }

    public ByteRegisterMemory asByteRegisterMemory() { // TODO: move the creation to cpu memory module?
        return new ByteRegisterMemory("PPUREGS",
                sint(MemoryMap.PPU_REGISTERS_START), // FIXME: this is cpu map specific offset
                new ByteRegister[] {
                ppuCtrl, ppuMask, ppuStatus,
                oamAddr, oamData,
                ppuScroll, ppuAddr, ppuData
        });
    }

}
