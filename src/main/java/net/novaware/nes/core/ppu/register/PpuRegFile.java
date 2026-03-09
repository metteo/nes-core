package net.novaware.nes.core.ppu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org
 */
public class PpuRegFile extends RegisterFile {

    public CycleCounter cycleCounter = new CycleCounter("PPUCC");

    // TODO: enforce R / W / RW / Wx2 specifics
    private ByteRegister ppuCtrl = new ByteRegister("PPUCTRL"); // 0x2000
    private ByteRegister ppuMask = new ByteRegister("PPUMASK"); // ...
    private ByteRegister ppuStatus = new ByteRegister("PPUSTATUS"); // TODO: use a dedicated register type

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

    @Inject
    public PpuRegFile() {
        super("PPU_REG");

        // TODO: inject with all the registers from module instead of creating them here

        dataRegisters = List.of(
                ppuCtrl, ppuMask, ppuStatus,
                oamAddr, oamData,
                ppuScroll, ppuAddr, ppuData,
                v, t, x, w
        );

        addressRegisters = List.of(ppuScrollFull, ppuAddrFull);
    }

    public ByteRegister[] getCpuRegisters() {
        return new ByteRegister[]{
                ppuCtrl, ppuMask, ppuStatus,
                oamAddr, oamData,
                ppuScroll, ppuAddr, ppuData
        };
    }

}
