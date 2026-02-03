package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

@BoardScope
public class ArithmeticLogic implements Unit {

    @Used
    private CpuRegisters registers;

    @Inject
    public ArithmeticLogic(CpuRegisters registers) {
        this.registers = registers;
    }

    public void bitwiseOr(@Unsigned byte operand) { // TODO: make package, when CU is here
        @Unsigned byte a = registers.accumulator.get();

        int result = uint(operand) | uint(a);

        registers.accumulator.setAsByte(result);
        registers.status
                .setZero(result == 0)
                .setNegative((result & (1 << 7)) > 0);
    }

    public void bitwiseAnd(@Unsigned byte operand) {
        @Unsigned byte a = registers.accumulator.get();

        int result = uint(a) & uint(operand);

        registers.accumulator.setAsByte(result);
        registers.status
                .setZero(result == 0)
                .setNegative((result & (1 << 7)) > 0);
    }

    public @Unsigned byte rotateLeft(@Unsigned byte data) {
        int oldCarry = registers.status.getCarry() ? 0x1 : 0;

        int newData = (uint(data) << 1) | oldCarry; // modify
        boolean newCarry = (newData & (1 << 8)) > 0;

        registers.status
                .setCarry(newCarry)
                .setZero(newData == 0)
                .setNegative((newData & (1 << 7)) > 0);

        return ubyte(newData);
    }
}
