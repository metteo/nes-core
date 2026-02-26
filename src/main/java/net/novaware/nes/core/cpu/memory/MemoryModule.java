package net.novaware.nes.core.cpu.memory;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.SystemBus;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.CycleCounter;

import static net.novaware.nes.core.cpu.CpuModule.CPU_CYCLE_COUNTER;

@Module
public interface MemoryModule {

    String CPU_BUS = "cpuBus";
    String STACK_SEGMENT = "stackSegment";

    @Provides
    @BoardScope
    @Named(CPU_BUS)
    static MemoryBus provideMemoryBus(@Named(CPU_CYCLE_COUNTER) CycleCounter cycleCounter) {
        return new SystemBus(cycleCounter);
    }

    @Provides
    @BoardScope
    @Named(STACK_SEGMENT)
    static AddressRegister provideStackSegment(CpuRegisters registers) {
        AddressRegister stackSegment = registers.getStackSegment();
        stackSegment.set(MemoryMap.STACK_SEGMENT_START);
        return stackSegment;
    }
}
