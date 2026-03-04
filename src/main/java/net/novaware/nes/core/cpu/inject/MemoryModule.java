package net.novaware.nes.core.cpu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.cpu.memory.CpuBus;
import net.novaware.nes.core.cpu.memory.MemoryMap;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.RecordingBus;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.SegmentRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ZP;
import static net.novaware.nes.core.util.UTypes.ushort;

@Module
public interface MemoryModule { // TODO: prefix class name with cpu?

    @Provides
    @BoardScope
    @CpuVar(BUS)
    static MemoryBus provideMemoryBus(
            CoreConfig config,
            @CpuVar(CC) CycleCounter cycleCounter
    ) {
        return switch(config.getCpuBusType()) {
            case RECORDING -> new RecordingBus(cycleCounter);
            case STANDARD -> new CpuBus(cycleCounter);
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

        return stackSegment;
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
