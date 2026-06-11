package net.novaware.nes.core.board.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.apu.register.ApuRegFile;
import net.novaware.nes.core.config.BorderRegion;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.ppu.memory.DisplayMemory;
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

    @Provides
    @BoardScope
    static DisplayMemory provideDisplayMemory(CoreConfig config) {
        VideoStandard vs = config.getVideoStandard();
        BorderRegion br = BorderRegion.of(vs);

        // TODO: fill with 0xFF on reset?
        return new DisplayMemory("DISPLAY",
            br.getTop() + vs.getActiveHeight() + br.getBottom(),
            br.getLeft() + vs.getActiveWidth() + br.getRight()
        );
    }

    // TODO: add the rest of memory mapped devices
}
