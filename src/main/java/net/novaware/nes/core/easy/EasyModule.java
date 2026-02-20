package net.novaware.nes.core.easy;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.memory.MemoryBus;

import static net.novaware.nes.core.cpu.memory.MemoryModule.CPU_BUS;

@Module
public interface EasyModule {

    @Provides
    @BoardScope
    @Named(CPU_BUS)
    static MemoryBus provideMemoryBus(CpuRegisters registers) {
        registers.getStackSegment().set(EasyMap.STACK_SEGMENT_START);
        MemoryBus bus = new EasyBus();
        return bus;
    }
}
