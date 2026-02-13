package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.register.StackPointer;
import net.novaware.nes.core.cpu.register.Status;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.DataRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class StackEngine implements Unit {

    private final CpuRegisters registers;
    private final MemoryMgmt mmu;

    @Inject
    public StackEngine (
        CpuRegisters registers,
        MemoryMgmt mmu
    ) {
        this.registers = registers;
        this.mmu = mmu;
    }

    void push(DataRegister register) {
        push(register.get());
    }

    void push(AddressRegister register) {
        push(register.high());
        push(register.low());
    }

    void push(@Unsigned short address) {
        int addrVal = uint(address);

        push(ubyte((addrVal & 0xFF00) >> 8));
        push(ubyte(addrVal & 0xFF));
    }

    void push(@Unsigned byte data) {
        final StackPointer sp = registers.sp();

        mmu.specifyAnd(sp.address())
                .writeByte(data);

        sp.decrement();
    }

    void pull(DataRegister register) {
        final StackPointer sp = registers.sp();

        sp.increment();

        @Unsigned byte data = mmu.specifyAnd(sp.address())
                .readByte();

        register.set(data);
    }

    void pushStatus() {
        final StackPointer sp = registers.sp();

        Status status = registers.status().get()
                .setBreak(true);

        @Unsigned byte data = status.get();

        mmu.specifyAnd(sp.address())
                .writeByte(data);

        sp.decrement();
    }

    void pullStatus() {
        final StackPointer sp = registers.sp();

        sp.increment();

        @Unsigned byte data = mmu.specifyAnd(sp.address())
                .readByte();

        Status status = registers.status().get();
        status.set(data);

        registers.status().set(status);
    }

    // JSR, RTS
}
