package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PC;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * Subunit of Control Unit responsible for handling control flow instructions
 * jumps, calls, returns, branches
 */
@BoardScope
public class ControlFlow implements Unit {

    @Used private final ShortRegister prefetchAddress;
    @Used private final ShortRegister programCounter;
    @Used private final DelegatingRegister decodedOperand;
    @Used private final IntegerCounter cycleCounter;
    @Used private final IntegerCounter instructionCycle;
    @Used private final StackEngine stackEngine;

    @Inject
    public ControlFlow(
            @CpuVar(PA) ShortRegister prefetchAddress,
            @CpuVar(PC) ShortRegister programCounter,
            @CpuVar(DO) DelegatingRegister decodedOperand,
            @CpuVar(CC) IntegerCounter cycleCounter,
            @CpuVar(IC) IntegerCounter instructionCycle,
            StackEngine stackEngine
    ) {
        this.prefetchAddress = prefetchAddress;
        this.programCounter = programCounter;
        this.decodedOperand = decodedOperand;
        this.cycleCounter = cycleCounter;
        this.instructionCycle = instructionCycle;
        this.stackEngine = stackEngine;
    }

    /* package */ void branchIf(boolean condition) {
        @Unsigned short jumpAddress = decodedOperand.getAddress();
        @Unsigned short currentPc = programCounter.get();

        int jumpAddressHi = sint(jumpAddress) >> 8;
        int pcHi = programCounter.highAsInt();
        boolean pageChange = jumpAddressHi != pcHi;

        cycleCounter.maybeIncrement(condition);
        cycleCounter.maybeIncrement(condition && pageChange);

        instructionCycle.maybeIncrement(condition);
        instructionCycle.maybeIncrement(condition && pageChange);

        programCounter.set(condition ? jumpAddress : currentPc); // hopefully cmov
    }

    /* package */ void jumpTo() {
        programCounter.set(decodedOperand.getAddress());
    }

    public void call() {
        stackEngine.peek(); // internal cycle

        int returnAddress = prefetchAddress.getAsInt() + 2; // pc is already +3 at this point

        stackEngine.push(ushort(returnAddress));

        programCounter.set(decodedOperand.getAddress());
    }

    public void returnFromCall() {                         // Cycles
                                                           // 1, 2 - opcode, ignored operand
        stackEngine.pull(programCounter);                  // 3, 4 - pull PCL, PCH

        int newPc = programCounter.getAsInt() + 1;
        stackEngine.peek();                                // 5 - inc PC

        programCounter.set(ushort(newPc));
        cycleCounter.increment();                          // 6 - set PC
        instructionCycle.increment();
    }
}
