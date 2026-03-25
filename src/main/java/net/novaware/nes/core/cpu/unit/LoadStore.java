package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PS;
import static net.novaware.nes.core.util.UTypes.sint;

@BoardScope
public class LoadStore implements Unit {

    @Used private final StatusRegister status;
    @Used private final DelegatingRegister decodedOperand;
    private final CycleCounter cycleCounter;

    @Inject
    public LoadStore(
            @CpuVar(PS) StatusRegister status,
            @CpuVar(DO) DelegatingRegister decodedOperand,
            @CpuVar(CC) CycleCounter cycleCounter
            ) {
        this.status = status;
        this.decodedOperand = decodedOperand;
        this.cycleCounter = cycleCounter;
    }

    void load(DataRegister register) {
        @Unsigned byte data = decodedOperand.getData();

        register.set(data);

        int dataVal = sint(data);

        // TODO: make a utility?
        status.setZero(dataVal == 0)
                .setNegative((dataVal & 0x80) != 0);
    }

    void store(DataRegister register) {
        @Unsigned byte data = register.get();

        // FIXME: it should be a bus read of uncorrected address
        cycleCounter.maybeIncrement(decodedOperand.isSTAIndexed() && !decodedOperand.isPageCrossed());
        decodedOperand.setSTAIndexed(false);

        decodedOperand.setData(data);
    }
}
