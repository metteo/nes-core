package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.ShortRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.MA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.MD;

/**
 * MMU
 */
@BoardScope
public class MemoryMgmt implements Unit { // TODO: get rid of this unit and move the registers to the bus.

    private final ShortRegister memoryAddress;
    private final ByteRegister memoryData;

    private final MemoryBus memoryBus;

    @Inject
    public MemoryMgmt(
        @CpuVar(MA) ShortRegister memoryAddress,
        @CpuVar(MD) ByteRegister memoryData,
        @CpuVar(BUS) MemoryBus memoryBus
    ) {
        this.memoryAddress = memoryAddress;
        this.memoryData = memoryData;
        this.memoryBus = memoryBus;
    }

    public MemoryMgmt specifyAnd(@Unsigned short address) {
        memoryAddress.set(address);
        memoryBus.access(address);

        return this;
    }

    public @Unsigned byte readByte() {
        @Unsigned byte data = memoryBus.read().data();
        memoryData.set(data);

        return data;
    }

    public void writeByte(@Unsigned byte data) {
        memoryData.set(data);
        memoryBus.write().data(data);
    }
}
