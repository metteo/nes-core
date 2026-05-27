package net.novaware.nes.core.board.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.apu.register.ApuRegFile;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.ppu.memory.PpuMemDevice;

import static net.novaware.nes.core.cpu.inject.CpuVarName.ACR;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.APU_REGISTERS_START;

@Module
public interface BoardMemModule {

    @Binds
    @BoardScope
    @CpuVar(PPU)
    MemoryDevice.ReadWrite bindPpuMemDevice(PpuMemDevice ppuMemDevice);

    @Provides
    @BoardScope
    @CpuVar(ACR)
    static MemoryDevice.WriteOnly provideApuRegs(ApuRegFile apuRegFile) {
        return new ByteRegisterMemory(
                "APU.REGS",
                APU_REGISTERS_START, APU_REGISTERS_END,
                apuRegFile.getCpuRegisters()
        );
    }

    // TODO: add the rest of memory mapped devices
}
