package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.memory.MemoryBus;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.MemoryModule.CPU_BUS;

@BoardScope
public class MemoryMgmt implements Unit {

    private final CpuRegisters registers;

    private final MemoryBus memoryBus;

    @Inject
    public MemoryMgmt(
        CpuRegisters registers,
        @Named(CPU_BUS) MemoryBus memoryBus
    ) {
        this.registers = registers;
        this.memoryBus = memoryBus;
    }

    public MemoryMgmt specifyAnd(@Unsigned short address) {
        registers.mar().set(address);
        memoryBus.specify(address);

        return this;
    }

    public @Unsigned byte readByte() {
        @Unsigned byte data = memoryBus.readByte();
        registers.mdr().set(data);

        return data;
    }

    public void writeByte(@Unsigned byte data) {
        registers.mdr().set(data);
        memoryBus.writeByte(data);
    }
}
