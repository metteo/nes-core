package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.register.Status;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.DataRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class StackEngine implements Unit {

    private final AddressRegister stackSegment;
    private final DataRegister stackPointer;
    private final StatusRegister status;

    private final MemoryMgmt mmu;

    @Inject
    public StackEngine (
        CpuRegisters registers,
        MemoryMgmt mmu
    ) {
        this.stackSegment = registers.getStackSegment();
        this.stackPointer = registers.getStackPointer();
        this.status = registers.getStatus();

        this.mmu = mmu;
    }

    private void increment() {
        int sp = stackPointer.getAsInt();
        stackPointer.setAsByte(sp + 1);
    }

    private void decrement() {
        int sp = stackPointer.getAsInt();
        stackPointer.setAsByte(sp - 1);
    }

    private @Unsigned short address() {
        return ushort(addressInt());
    }

    private int addressInt() {
        return stackSegment.getAsInt() + stackPointer.getAsInt();
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
        mmu.specifyAnd(address())
                .writeByte(data);

        decrement();
    }

    @Unsigned byte pull() {
        increment();

        @Unsigned byte data = mmu.specifyAnd(address())
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
        Status s = status.get().setBreak(true);

        @Unsigned byte data = s.get();

        mmu.specifyAnd(address())
                .writeByte(data);

        decrement();
    }

    void pullStatus() {
        increment();

        @Unsigned byte data = mmu.specifyAnd(address())
                .readByte();

        Status s = status.get();
        s.set(data);

        status.set(s);
    }
}
