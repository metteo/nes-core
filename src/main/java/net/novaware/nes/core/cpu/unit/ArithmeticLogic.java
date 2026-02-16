package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.function.IntBinaryOperator;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

@BoardScope
public class ArithmeticLogic implements Unit {

    @Used
    private final CpuRegisters registers;

    @Inject
    public ArithmeticLogic(CpuRegisters registers) {
        this.registers = registers;
    }

    public void addWithCarry(@Unsigned byte data) {
        int prevCarry = registers.status().getCarry() ? 1 : 0;

        int a = registers.a().getAsInt();
        int aSign = a >> 7;

        int dataVal = uint(data);
        int dataSign = dataVal >> 7;

        int result = a + dataVal + prevCarry;
        int byteResult = result & 0xFF;

        int resultSign = byteResult >> 7;

        registers.a().setAsByte(byteResult);
        registers.status()
                .setCarry(result > 0xFF)
                .setZero(byteResult == 0)
                .setOverflow(resultSign != aSign && resultSign != dataSign)
                .setNegative(resultSign == 1);
    }

    public void subtractWithBorrow(@Unsigned byte data) {
        int prevBorrow = registers.status().getBorrow() ? 1 : 0;

        int a = registers.a().getAsInt();
        int aSign = a >> 7;

        int dataVal = uint(data);
        int dataSign = dataVal >> 7;

        int result = a - dataVal - prevBorrow;
        int byteResult = result & 0xFF;

        int resultSign = byteResult >> 7;
        registers.a().setAsByte(byteResult);
        registers.status()
                .setBorrow(result < 0)
                .setZero(byteResult == 0)
                .setOverflow(resultSign != aSign && resultSign == dataSign)
                .setNegative(resultSign == 1);
    }

    public @Unsigned byte incrementMemory(@Unsigned byte data) {
        return incrementMemory(data, 1);
    }

    public @Unsigned byte decrementMemory(@Unsigned byte data) {
        return incrementMemory(data, -1);
    }

    private @Unsigned byte incrementMemory(@Unsigned byte data, int by) {
        int dataVal = uint(data);

        int result = dataVal + by;
        int resultByte = result & 0xFF;

        // TODO: status register gets updated here but memory outside (in readModifyWrite)
        registers.status()
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0);

        return ubyte(resultByte);
    }

    public void incrementX() {
        incrementRegister(registers.x(), 1);
    }

    public void decrementX() {
        incrementRegister(registers.x(), -1);
    }

    public void incrementY() {
        incrementRegister(registers.y(), 1);
    }

    public void decrementY() {
        incrementRegister(registers.y(), -1);
    }

    private void incrementRegister(DataRegister register, int by) {
        int val = register.getAsInt();

        int result = val + by;
        int resultByte = result & 0xFF;

        register.setAsByte(resultByte);

        registers.status()
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0); // TODO: extract, repeats a lot
    }

    public void bitwiseOp(@Unsigned byte operand, IntBinaryOperator operator) {
        @Unsigned byte a = registers.a().get();

        int result = operator.applyAsInt(uint(a), uint(operand));

        registers.a().setAsByte(result);
        registers.status()
                .setZero(result == 0)
                .setNegative((result & (1 << 7)) > 0);
    }

    public void bitwiseAnd(@Unsigned byte operand) {
        bitwiseOp(operand, (a, b) -> a & b);
    }

    public void bitwiseOr(@Unsigned byte operand) { // TODO: make package, when CU is here
        bitwiseOp(operand, (a, b) -> a | b);
    }

    public void bitwiseXor(@Unsigned byte operand) {
        bitwiseOp(operand, (a, b) -> a ^ b);
    }

    public void bitTest(@Unsigned byte data) {
        @Unsigned byte a = registers.a().get();

        int aVal = uint(a);
        int dataVal = uint(data);

        int result = aVal & dataVal;
        int resultByte = result & 0xFF;

        registers.status()
                .setZero(result == 0)
                .setOverflow((resultByte & (1 << 6)) >> 6 == 1)
                .setNegative((resultByte & (1 << 7)) >> 7 == 1);
    }

    public void compareA(@Unsigned byte data) {
        compareRegister(registers.a(), data);
    }

    public void compareX(@Unsigned byte data) {
        compareRegister(registers.x(), data);
    }

    public void compareY(@Unsigned byte data) {
        compareRegister(registers.y(), data);
    }

    private void compareRegister(DataRegister register, @Unsigned byte data) {
        @Unsigned byte reg = register.get();

        int regVal = uint(reg);
        int dataVal = uint(data);

        int result = regVal - dataVal;
        int resultByte = result & 0xFF;


        registers.status()
                .setBorrow(result < 0)
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0);
    }

    public @Unsigned byte rotateLeft(@Unsigned byte data) {
        int oldCarry = registers.status().getCarry() ? 1 : 0;

        int dataVal = uint(data);

        int result = (dataVal << 1) | oldCarry; // modify
        boolean newCarry = (result & (1 << 8)) > 0;
        int resultByte = result & 0xFF;

        registers.status()
                .setCarry(newCarry)
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0);

        return ubyte(result);
    }

    public @Unsigned byte rotateRight(@Unsigned byte data) {
        int oldCarry = registers.status().getCarry() ? (1 << 7) : 0;
        boolean newCarry = (data & 0b1) > 0;
        int newData = (uint(data) >> 1) | oldCarry; // modify // FIXME: consider >> vs >>>

        registers.status()
                .setCarry(newCarry)
                .setZero(newData == 0)
                .setNegative((newData & (1 << 7)) > 0);

        return ubyte(newData);
    }

    public @Unsigned byte arithmeticShiftLeft(@Unsigned byte data) {
        int dataVal = uint(data);

        int result = dataVal << 1;
        int resultByte = result & 0xFF;

        registers.status()
                .setCarry(((result & (1 << 8)) > 0))
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0);

        return ubyte(resultByte);
    }

    public @Unsigned byte logicalShiftRight(@Unsigned byte data) {
        int dataVal = uint(data);

        int result = dataVal >> 1;
        int resultByte = result & 0xFF;

        registers.status()
                .setCarry((dataVal & 0b1) > 0)
                .setZero(resultByte == 0)
                .setNegative(false);

        return ubyte(resultByte);
    }
}
