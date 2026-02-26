package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.memory.MemoryModule;
import net.novaware.nes.core.cpu.register.Status;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.DataRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class StackEngine implements Unit {

    private final AddressRegister stackSegment;
    private final DataRegister stackPointer;
    private final StatusRegister status;

    private final MemoryMgmt mmu;

    @Inject
    public StackEngine (
        @Named(MemoryModule.STACK_SEGMENT) AddressRegister stackSegment,
        CpuRegisters registers,
        MemoryMgmt mmu
    ) {
        this.stackSegment = stackSegment; // important, injection initializes value
        this.stackPointer = registers.getStackPointer();
        this.status = registers.getStatus();

        this.mmu = mmu;
    }

    public void initialize() {
        assertState(stackSegment.getAsInt() != 0, "Stack Segment points to Zero Page");
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
        @Unsigned byte data = pull();
        int dataVal = sint(data);

        register.set(data);

        status.setZero(dataVal == 0)
                .setNegative((dataVal & (1 << 7)) > 0);
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
