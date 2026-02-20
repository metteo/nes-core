package net.novaware.nes.core.cpu;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.register.CycleCounter;

@Module
public interface CpuModule {

    String CPU_CYCLE_COUNTER = "cpuCycleCounter";

    @Provides
    @BoardScope
    @Named(CPU_CYCLE_COUNTER)
    static CycleCounter provideCpuCycleCounter() {
        return new CycleCounter("CPUCC");
    }
}
