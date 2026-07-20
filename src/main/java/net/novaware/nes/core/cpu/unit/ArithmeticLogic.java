package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.A;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.X;
import static net.novaware.nes.core.cpu.inject.CpuVarName.Y;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

@BoardScope
public class ArithmeticLogic implements Unit {

    @Used
    private final ByteRegister accumulator;

    private final ByteRegister indexX;
    private final ByteRegister indexY;
    @Used
    private final StatusRegister status;

    @Inject
    public ArithmeticLogic(
        @CpuVar(A) ByteRegister accumulator,
        @CpuVar(X) ByteRegister indexX,
        @CpuVar(Y) ByteRegister indexY,
        @CpuVar(PS) StatusRegister status
    ) {
        this.accumulator = accumulator;
        this.indexX = indexX;
        this.indexY = indexY;
        this.status = status;
    }

    /**
     * // TODO: implement decimal mode, but hide it behind EFlags.disableDecimal
     * @see <a href="https://6502.org/tutorials/decimal_mode.html">Decimal Mode</a>
     */
    public void addWithCarry(@Unsigned byte data) {
        int prevCarry = status.getCarry() ? 1 : 0;

        int a = accumulator.getAsInt();
        int aSign = a >> 7;

        int dataVal = sint(data);
        int dataSign = dataVal >> 7;

        int result = a + dataVal + prevCarry;
        int byteResult = result & 0xFF;

        int resultSign = byteResult >> 7;

        boolean overflow = resultSign != aSign && resultSign != dataSign;

        accumulator.setAsByte(byteResult);
        status.setCarry(result > 0xFF)
                .setZero(byteResult == 0)
                .setOverflow(overflow)
                .setNegative(resultSign == 1);
    }

    void subtractWithBorrow(@Unsigned byte data) {
        int prevBorrow = status.getBorrow() ? 1 : 0;

        int a = accumulator.getAsInt();
        int aSign = a >> 7;

        int dataVal = sint(data);
        int dataSign = dataVal >> 7;

        int result = a - dataVal - prevBorrow;
        int byteResult = result & 0xFF;

        int resultSign = byteResult >> 7;
        accumulator.setAsByte(byteResult);

        boolean overflow = resultSign != aSign && resultSign == dataSign;

        status.setBorrow(result < 0)
                .setZero(byteResult == 0)
                .setOverflow(overflow)
                .setNegative(resultSign == 1);
    }

    @Unsigned byte incrementMemory(@Unsigned byte data) {
        return incrementMemory(data, 1);
    }

    @Unsigned byte decrementMemory(@Unsigned byte data) {
        return incrementMemory(data, -1);
    }

    @Unsigned byte incrementMemory(@Unsigned byte data, int by) {
        int dataVal = sint(data);

        int result = dataVal + by;

        // TODO: status register gets updated here but memory outside (in readModifyWrite)
        status.maybeZeroOrNegative(result);

        return ubyte(result);
    }

    void incrementX() {
        incrementRegister(indexX, 1);
    }

    void decrementX() {
        incrementRegister(indexX, -1);
    }

    void incrementY() {
        incrementRegister(indexY, 1);
    }

    void decrementY() {
        incrementRegister(indexY, -1);
    }

    private void incrementRegister(DataRegister register, int by) {
        int val = register.getAsInt();

        int result = val + by;

        register.setAsByte(result);
        status.maybeZeroOrNegative(result);
    }

    void bitwiseAnd(@Unsigned byte operand) {
        int result = accumulator.getAsInt() & sint(operand);

        accumulator.setAsByte(result);
        status.maybeZeroOrNegative(result);
    }

    void bitwiseOr(@Unsigned byte operand) {
        int result = accumulator.getAsInt() | sint(operand);

        accumulator.setAsByte(result);
        status.maybeZeroOrNegative(result);
    }

    void bitwiseXor(@Unsigned byte operand) {
        int result = accumulator.getAsInt() ^ sint(operand);

        accumulator.setAsByte(result);
        status.maybeZeroOrNegative(result);
    }

    void bitTest(@Unsigned byte data) {
        @Unsigned byte a = accumulator.get();

        int aVal = sint(a);
        int dataVal = sint(data);

        int result = aVal & dataVal & 0xFF;

        status.setZero(result == 0)
                .setOverflow((dataVal & (1 << 6)) != 0)
                .setNegative((dataVal & (1 << 7)) != 0);
    }

    void compareA(@Unsigned byte data) {
        compareRegister(accumulator, data);
    }

    void compareX(@Unsigned byte data) {
        compareRegister(indexX, data);
    }

    void compareY(@Unsigned byte data) {
        compareRegister(indexY, data);
    }

    private void compareRegister(DataRegister register, @Unsigned byte data) {
        @Unsigned byte reg = register.get();

        int regVal = sint(reg);
        int dataVal = sint(data);

        int result = regVal - dataVal;

        status.setBorrow(result < 0)
                .maybeZeroOrNegative(result);
    }

    @Unsigned byte rotateLeft(@Unsigned byte data) {
        int oldCarry = status.getCarry() ? 1 : 0;

        int dataVal = sint(data);

        int result = (dataVal << 1) | oldCarry; // modify
        boolean newCarry = (result & (1 << 8)) > 0;
        int resultByte = result & 0xFF;

        status.setCarry(newCarry)
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0);

        return ubyte(result);
    }

    @Unsigned byte rotateRight(@Unsigned byte data) {
        int oldCarry = status.getCarry() ? (1 << 7) : 0;
        boolean newCarry = (data & 0b1) > 0;
        int newData = (sint(data) >> 1) | oldCarry; // modify // FIXME: consider >> vs >>>

        status.setCarry(newCarry)
                .setZero(newData == 0)
                .setNegative((newData & (1 << 7)) > 0);

        return ubyte(newData);
    }

    @Unsigned byte arithmeticShiftLeft(@Unsigned byte data) {
        int dataVal = sint(data);

        int result = dataVal << 1;
        int resultByte = result & 0xFF;

        status.setCarry(((result & (1 << 8)) > 0))
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0);

        return ubyte(resultByte);
    }

    @Unsigned byte logicalShiftRight(@Unsigned byte data) {
        int dataVal = sint(data);

        int result = dataVal >> 1;
        int resultByte = result & 0xFF;

        status.setCarry((dataVal & 0b1) > 0)
                .setZero(resultByte == 0)
                .setNegative(false);

        return ubyte(resultByte);
    }

    void transfer(DataRegister src, DataRegister dst) {
        @Unsigned byte data = src.get();
        dst.set(data);

        status.maybeZeroOrNegative(sint(data));
    }
}
