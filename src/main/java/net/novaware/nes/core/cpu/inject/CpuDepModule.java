package net.novaware.nes.core.cpu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.IntegerCounter;

import static net.novaware.nes.core.cpu.inject.CpuVarName.ACR;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_SIZE;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_START;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_MIRROR_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_SIZE;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_START;
import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SC;

/**
 * CPU Required Dependencies module. Should be provided by the board config.
 * This module is used as a stub when running standalone CPU.
 */
@Module
public interface CpuDepModule {

    // TODO: get rid of device specific injection points. Instead provide collection of memory devices that are assembled and attached?
    // TODO: careful with MemoryPage assembly
    @Provides
    @BoardScope
    @CpuVar(PPU)
    static MemoryDevice.ReadWrite provideStubPpuMemDevice() {
        return new PhysicalMemory(PPU.doc(), PPU_REGISTERS_START, PPU_REGISTERS_MIRROR_END, PPU_REGISTERS_SIZE);
    }

    // FIXME: CPU should not have hard dependency on scanline / dot counters
    @Provides
    @BoardScope
    @PpuVar(SC)
    static IntegerCounter provideScanLineCounter() {
        return new IntegerCounter(SC.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(DC)
    static IntegerCounter provideDotCounter() {
        return new IntegerCounter(DC.doc());
    }

    @Provides
    @BoardScope
    @DmaVar(OAM)
    static ByteRegister provideOamDmaRegister() {
        return new ByteRegister("OAMDMA"); // 0x4014
    }

    @Provides
    @BoardScope
    @CpuVar(ACR)
    static MemoryDevice.WriteOnly provideApuRegs() {
        return new PhysicalMemory("APU.REGS", APU_REGISTERS_START, APU_REGISTERS_END, APU_REGISTERS_SIZE);
    }
}
