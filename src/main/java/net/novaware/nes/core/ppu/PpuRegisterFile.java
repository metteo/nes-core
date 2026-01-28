package net.novaware.nes.core.ppu;

import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.ShortRegister;
import org.jspecify.annotations.Nullable;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org
 */
public class PpuRegisterFile {

    // TODO: enforce R / W / RW / Wx2 specifics
    private ByteRegister ppuCtrl = new ByteRegister("PPUCTRL"); // 0x2000
    private ByteRegister ppuMask = new ByteRegister("PPUMASK"); // ...
    private ByteRegister ppuStatus = new ByteRegister("PPUSTATUS");
    private ByteRegister oamAddr = new ByteRegister("OAMADDR");
    private ByteRegister oamData = new ByteRegister("OAMDATA");
    private ByteRegister ppuScroll = new ByteRegister("PPUSCROLL");
    private ByteRegister ppuAddr = new ByteRegister("PPUADDR");
    private ByteRegister ppuData = new ByteRegister("PPUDATA"); // 0x2007

    private ShortRegister ppuScrollFull = new ShortRegister("PPUSCROLL_FULL");
    private ShortRegister ppuAddrFull = new ShortRegister("PPUADDR_FULL");

    private @Nullable ByteRegister oamDma = null; // 0x4014

    public ByteRegisterMemory asByteRegisterMemory() {
        return new ByteRegisterMemory("PPUREGS", new ByteRegister[] {
                ppuCtrl, ppuMask, ppuStatus, oamAddr, oamData, ppuScroll, ppuAddr, ppuData
        });
    }

}
