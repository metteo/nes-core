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

    @FunctionalInterface
    interface ByteUnaryOperator {
        @Unsigned byte apply(@Unsigned byte data);
    }

    @Used
    private CpuRegisters registers;

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
        throw new UnsupportedOperationException("no implemented");
    }

    public @Unsigned byte decrementMemory(@Unsigned byte data) {
        throw new UnsupportedOperationException("no implemented");
    }

    public void incrementX() {

    }

    public void decrementX() {

    }

    public void incrementY() {

    }

    public void decrementY() {

    }

    public void bitwiseAnd(@Unsigned byte operand) {
        @Unsigned byte a = registers.a().get();

        int result = uint(a) & uint(operand);

        registers.a().setAsByte(result);
        registers.status()
                .setZero(result == 0)
                .setNegative((result & (1 << 7)) > 0);
    }

    public void bitwiseOr(@Unsigned byte operand) { // TODO: make package, when CU is here
        @Unsigned byte a = registers.a().get();

        int result = uint(operand) | uint(a);

        registers.a().setAsByte(result);
        registers.status()
                .setZero(result == 0)
                .setNegative((result & (1 << 7)) > 0);
    }

    public void bitwiseXor() {

    }

    public void compareA() {

    }

    public void compareX() {

    }

    public void compareY() {

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
        int newData = (uint(data) >> 1) | oldCarry; // modify

        registers.status()
                .setCarry(newCarry)
                .setZero(newData == 0)
                .setNegative((newData & (1 << 7)) > 0);

        return ubyte(newData);
    }

    public @Unsigned byte arithmeticShiftLeft(@Unsigned byte data) {
        throw new UnsupportedOperationException("not implemented");
    }

    public @Unsigned byte logicalShiftRight(@Unsigned byte data) {
        throw new UnsupportedOperationException("not implemented");
    }
}
