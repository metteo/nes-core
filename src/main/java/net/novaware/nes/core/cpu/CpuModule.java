package net.novaware.nes.core.cpu;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.SystemBus;
import net.novaware.nes.core.register.CycleCounter;

@Module
public interface CpuModule {

    String CPU_CYCLE_COUNTER = "cpuCycleCounter";
    String CPU_BUS = "cpuBus";

    @Provides
    @BoardScope
    @Named(CPU_CYCLE_COUNTER)
    static CycleCounter provideCpuCycleCounter() {
        return new CycleCounter("CPUCC");
    }

    @Provides
    @BoardScope
    @Named(CPU_BUS)
    static MemoryBus provideMemoryBus(@Named(CPU_CYCLE_COUNTER) CycleCounter cycleCounter) {
        return new SystemBus(cycleCounter);
    }
}
