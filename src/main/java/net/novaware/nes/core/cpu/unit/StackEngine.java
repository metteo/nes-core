package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.register.Status;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.BooleanLatch;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.SegmentRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.ID;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;
import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class StackEngine implements Unit {

    private final SegmentRegister stackSegment;
    private final DataRegister stackPointer;
    private final StatusRegister status;
    private final BooleanLatch interruptDisabled;

    private final MemoryMgmt mmu;

    @Inject
    public StackEngine (
        @CpuVar(SS) SegmentRegister stackSegment,
        @CpuVar(ID) BooleanLatch interruptDisabled,
        CpuRegFile registers,
        MemoryMgmt mmu
    ) {
        this.stackSegment = stackSegment;
        this.interruptDisabled = interruptDisabled;
        this.stackPointer = registers.getStackPointer();
        this.status = registers.getStatus();

        this.mmu = mmu;
    }

    public void initialize() {
        assertState(stackSegment.getStartAsInt() != 0, "Stack Segment points to Zero Page");
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
        return stackSegment.getStartAsInt() + stackPointer.getAsInt();
    }

    void peek() {
        mmu.specifyAnd(address());
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

        status.maybeZeroOrNegative(dataVal);
    }

    void pull(AddressRegister register) {
        // order matters, low first
        register.low(pull());
        register.high(pull());
    }

    void pushStatus(boolean brk) {
        Status toPush = status.get().setBreak(brk);

        @Unsigned byte data = toPush.get();

        mmu.specifyAnd(address())
                .writeByte(data);

        decrement();
    }

    private Status pullStatus0() {
        increment();

        @Unsigned byte data = mmu.specifyAnd(address())
                .readByte();

        Status workingCopy = status.get();
        workingCopy.set(data);

        return workingCopy;
    }

    public void pullStatus() {
        status.set(pullStatus0());
    }

    public void pullStatusWithDelayedInterruptDisable() {
        Status workingCopy = pullStatus0();

        interruptDisabled.delayedSet(workingCopy.isIrqDisabled());
        workingCopy.setIrqDisabled(status.isIrqDisabled());

        status.set(workingCopy);
    }
}
