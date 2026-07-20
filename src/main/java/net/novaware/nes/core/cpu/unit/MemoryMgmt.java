package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.memory.CpuBus;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.ShortRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.MA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.MD;

/**
 * MMU
 */
@BoardScope
public class MemoryMgmt implements Unit { // TODO: get rid of this unit and move the registers to the bus.

    private final ShortRegister memoryAddress;
    private final ByteRegister memoryData;

    private final CpuBus cpuBus;

    @Inject
    public MemoryMgmt(
        @CpuVar(MA) ShortRegister memoryAddress,
        @CpuVar(MD) ByteRegister memoryData,
        CpuBus cpuBus
    ) {
        this.memoryAddress = memoryAddress;
        this.memoryData = memoryData;
        this.cpuBus = cpuBus;
    }

    public MemoryMgmt specifyAnd(@Unsigned short address) {
        memoryAddress.set(address);
        cpuBus.access(address);

        return this;
    }

    public @Unsigned byte readByte() {
        @Unsigned byte data = cpuBus.read().data();
        memoryData.set(data);

        return data;
    }

    public void writeByte(@Unsigned byte data) {
        memoryData.set(data);
        cpuBus.write().data(data);
    }
}
