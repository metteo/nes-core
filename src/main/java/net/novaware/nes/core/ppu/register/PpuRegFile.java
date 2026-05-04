package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.RegisterFile;

import java.util.List;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org
 */
@BoardScope
public class PpuRegFile extends RegisterFile {

    public CycleCounter cycleCounter = new CycleCounter("PPUCC");

    private ByteRegister ppuCtrl = new ByteRegister("PPUCTRL"); // 0x2000
    private ByteRegister ppuMask = new ByteRegister("PPUMASK"); // ...
    private ByteRegister ppuStatus = new ByteRegister("PPUSTATUS");

    private ByteRegister oamAddr = new ByteRegister("OAMADDR");
    private ByteRegister oamData = new ByteRegister("OAMDATA");

    private ByteRegister ppuScroll = new ByteRegister("PPUSCROLL");
    private ByteRegister ppuAddr = new ByteRegister("PPUADDR");
    private ByteRegister ppuData = new ByteRegister("PPUDATA"); // 0x2007

    //@Inject
    public PpuRegFile() {
        super("PPU.REGS");

        // TODO: inject with all the registers from module instead of creating them here

        dataRegisters = List.of(
                ppuCtrl, ppuMask, ppuStatus,
                oamAddr, oamData,
                ppuScroll, ppuAddr, ppuData
        );
    }

    public ByteRegister[] getCpuRegisters() {
        return new ByteRegister[]{
                ppuCtrl, ppuMask, ppuStatus,
                oamAddr, oamData,
                ppuScroll, ppuAddr, ppuData
        };
    }

}
