package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.register.StackPointer;
import net.novaware.nes.core.cpu.register.Status;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.DataRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.sint;

@BoardScope
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
        int addrVal = sint(address);

        int addrHi = (addrVal & 0xFF00) >> 8;
        int addrLo = addrVal & 0xFF;

        push(ubyte(addrHi));
        push(ubyte(addrLo));
    }

    void push(@Unsigned byte data) {
        final StackPointer sp = registers.sp();

        mmu.specifyAnd(sp.address())
                .writeByte(data);

        sp.decrement();
    }

    @Unsigned byte pull() {
        final StackPointer sp = registers.sp();

        sp.increment();

        @Unsigned byte data = mmu.specifyAnd(sp.address())
                .readByte();

        return data;
    }

    void pull(DataRegister register) {
        register.set(pull());
    }

    void pull(AddressRegister register) {
        // order matters, low first
        register.low(pull());
        register.high(pull());
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
}
