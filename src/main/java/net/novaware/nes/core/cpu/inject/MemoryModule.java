package net.novaware.nes.core.cpu.inject;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.apu.register.ApuRegFile;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.cpu.memory.CpuBus;
import net.novaware.nes.core.cpu.memory.MemoryMap;
import net.novaware.nes.core.memory.ByteRegisterMemory;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.memory.RecordingBus;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.register.SegmentRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.APU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.OS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ZP;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

@Module
public interface MemoryModule { // TODO: prefix class name with cpu?

    @Provides
    @BoardScope
    @CpuVar(RAM)
    static MemoryDevice provideMemory() {
        return new PhysicalMemory(MemoryMap.RAM_SIZE, sint(MemoryMap.RAM_START));
    }

    @Provides
    @BoardScope
    @CpuVar(PPU)
    static MemoryDevice providePpuRegs(PpuRegFile ppuRegFile) {
        return new ByteRegisterMemory("PPU_REGS",
                sint(MemoryMap.PPU_REGISTERS_START),
                ppuRegFile.getCpuRegisters());
    }

    @Provides
    @BoardScope
    @CpuVar(APU)
    static MemoryDevice provideApuRegs(ApuRegFile apuRegFile) {
        return new ByteRegisterMemory("APU_REGS",
                sint(MemoryMap.APU_IO_REGISTERS_START),
                apuRegFile.getCpuRegisters());
    }

    @Provides
    @BoardScope
    @CpuVar(BUS)
    static MemoryBus provideMemoryBus(
        CoreConfig config,
        Lazy<RecordingBus> recordingBus,
        Lazy<CpuBus> cpuBus
    ) {
        return switch(config.getCpuBusType()) {
            case RECORDING -> recordingBus.get();
            case STANDARD -> cpuBus.get();
        };
    }

    @Provides
    @BoardScope
    @CpuVar(ZP)
    static SegmentRegister provideZeroPage() {
        SegmentRegister zeroPage = new SegmentRegister(ZP.name());
        zeroPage.setStart(MemoryMap.RAM_START);
        zeroPage.setLimit(ushort(0x00FF)); // TODO: move to memory map?

        return zeroPage;
    }

    @Provides
    @BoardScope
    @CpuVar(SS)
    static SegmentRegister provideStackSegment() {
        SegmentRegister stackSegment = new SegmentRegister(SS.name());
        stackSegment.setStart(MemoryMap.STACK_SEGMENT_START);
        stackSegment.setLimit(MemoryMap.STACK_SEGMENT_END);

        return stackSegment;
    }

    @Provides
    @BoardScope
    @CpuVar(OS)
    static SegmentRegister provideOamSegment() {
        SegmentRegister oamSegment = new SegmentRegister(OS.name());
        oamSegment.setStart(MemoryMap.OAM_SEGMENT_START);
        oamSegment.setLimit(MemoryMap.OAM_SEGMENT_END);

        return oamSegment;
    }

    @Provides
    @BoardScope
    @CpuVar(CS)
    static SegmentRegister provideCodeSegment() {
        SegmentRegister codeSegment = new SegmentRegister(CS.name());
        // FIXME: set start and limit in cartridge?

        return codeSegment;
    }

    @Provides
    @BoardScope
    @CpuVar(DS)
    static SegmentRegister provideDataSegment() {
        SegmentRegister dataSegment = new SegmentRegister(DS.name());
        // FIXME: set start and limit in cartridge?

        return dataSegment;
    }

    @Provides
    @BoardScope
    @CpuVar(ES)
    static SegmentRegister provideExtraSegment() {
        SegmentRegister extraSegment = new SegmentRegister(ES.name());
        // FIXME: set start and limit in cartridge?

        return extraSegment;
    }
}
