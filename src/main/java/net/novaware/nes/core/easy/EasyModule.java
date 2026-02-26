package net.novaware.nes.core.easy;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.memory.MemoryMap;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.AddressRegister;

import static net.novaware.nes.core.cpu.memory.MemoryModule.CPU_BUS;
import static net.novaware.nes.core.cpu.memory.MemoryModule.STACK_SEGMENT;

@Module
public interface EasyModule {

    @Provides
    @BoardScope
    @Named(CPU_BUS)
    static MemoryBus provideMemoryBus() {
        MemoryBus bus = new EasyBus();
        return bus;
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
