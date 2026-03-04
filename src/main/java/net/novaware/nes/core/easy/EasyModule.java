package net.novaware.nes.core.easy;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;

@Module
public interface EasyModule {

    @Provides
    @BoardScope
    @CpuVar(BUS)
    static MemoryBus provideMemoryBus() {
        MemoryBus bus = new EasyBus();
        return bus;
    }

    @Provides
    @BoardScope
    @CpuVar(SS)
    static SegmentRegister provideStackSegment() {
        SegmentRegister stackSegment = new SegmentRegister(SS.name());
        stackSegment.setStart(EasyMap.STACK_SEGMENT_START);

        return stackSegment;
    }
}
