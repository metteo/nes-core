package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PC;
import static net.novaware.nes.core.util.UTypes.sint;

/**
 * Subunit of Control Unit responsible for handling control flow instructions
 * jumps, calls, returns, branches
 */
@BoardScope
public class ControlFlow implements Unit {

    @Used private final ShortRegister programCounter;
    @Used private final DelegatingRegister decodedOperand;
    @Used private final CycleCounter cycleCounter;
    @Used private final StackEngine stackEngine;

    @Inject
    public ControlFlow(
            @CpuVar(PC) ShortRegister programCounter,
            @CpuVar(DO) DelegatingRegister decodedOperand,
            @CpuVar(CC) CycleCounter cycleCounter,
            StackEngine stackEngine
    ) {
        this.programCounter = programCounter;
        this.decodedOperand = decodedOperand;
        this.cycleCounter = cycleCounter;
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
        programCounter.set(condition ? jumpAddress : currentPc); // hopefully cmov
    }

    /* package */ void jumpTo() {
        programCounter.set(decodedOperand.getAddress());
    }

    public void call() {
        // NOTE: not pc+2 here, because reading absolute address already did it?
        stackEngine.push(programCounter);

        programCounter.set(decodedOperand.getAddress());
    }

    public void returnFromCall() {
        stackEngine.pull(programCounter);

        // FIXME: why?
        programCounter.setAsShort(programCounter.getAsInt());
    }
}
