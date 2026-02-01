package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.register.CpuRegisterFile;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

@BoardScope
public class ArithmeticLogic implements Unit {

    @Used
    private CpuRegisterFile registers;

    @Inject
    public ArithmeticLogic(CpuRegisterFile registers) {
        this.registers = registers;
    }

    public void bitwiseOr(@Unsigned byte memVal) { // TODO: make package, when CU is here
        @Unsigned byte a = registers.accumulator.get();

        int result = uint(memVal) | uint(a);

        registers.accumulator.setAsByte(result);
        registers.status
                .setZero(result == 0)
                .setNegative((result & (1 << 7)) > 0);
    }
}
