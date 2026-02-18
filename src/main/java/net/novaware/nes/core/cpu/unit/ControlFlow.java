package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_CYCLE_COUNTER;
import static net.novaware.nes.core.util.UnsignedTypes.sint;

/**
 * Subunit of Control Unit responsible for handling control flow instructions
 * jumps, calls, returns, branches
 */
@BoardScope
public class ControlFlow implements Unit {

    @Used private final CpuRegisters registers;
    @Used private final CycleCounter cycleCounter;
    @Used private final StackEngine stackEngine;
    
    @Inject
    public ControlFlow(
            CpuRegisters registers,
            @Named(CPU_CYCLE_COUNTER) CycleCounter cycleCounter,
            StackEngine stackEngine
    ) {
        this.registers = registers;
        this.cycleCounter = cycleCounter;
        this.stackEngine = stackEngine;
    }

    /* package */ void branchIf(boolean condition) {
        @Unsigned short jumpAddress = registers.dor().getAddress();
        @Unsigned short currentPc = registers.pc().get();

        int jumpAddressHi = sint(jumpAddress) >> 8;
        int pcHi = registers.pc().highAsInt();
        boolean pageChange = jumpAddressHi != pcHi;

        cycleCounter.maybeIncrement(condition);
        cycleCounter.maybeIncrement(condition && pageChange);
        registers.pc().set(condition ? jumpAddress : currentPc); // hopefully cmov
    }

    /* package */ void jumpTo() {
        registers.pc().set(registers.dor().getAddress());
    }

    public void call() {
        final AddressRegister pc = registers.pc();

        // NOTE: not pc+2 here, because reading absolute address already did it?
        stackEngine.push(pc);

        pc.set(registers.dor().getAddress());
    }

    public void returnFromCall() {
        AddressRegister pc = registers.pc();
        stackEngine.pull(pc);

        pc.setAsShort(pc.getAsInt() + 1);
    }
}
