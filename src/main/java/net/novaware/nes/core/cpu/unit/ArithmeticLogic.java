package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.function.IntBinaryOperator;

import static net.novaware.nes.core.cpu.inject.CpuVarName.A;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SOV;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

@BoardScope
public class ArithmeticLogic implements Unit {

    @Used
    private final CpuRegFile registers; // TODO: replace with direct register access

    @Used
    private final ByteRegister accumulator;

    @Used
    private final DelegatingRegister operand2; // TODO: rename to operand when 0 args methods done

    @Used
    private final StatusRegister status;

    @Used
    private final EdgeDetector setOverflow;

    @Inject
    public ArithmeticLogic(
            CpuRegFile registers,
            @CpuVar(A) ByteRegister accumulator,
            @CpuVar(DO) DelegatingRegister operand,
            @CpuVar(PS) StatusRegister status,
            @CpuVar(SOV) EdgeDetector setOverflow
    ) {
        this.registers = registers;
        this.accumulator = accumulator;
        this.operand2 = operand;
        this.status = status;
        this.setOverflow = setOverflow;
    }

    public void addWithCarry(@Unsigned byte data) { // TODO: implement decimal mode, but hide it behind EFlags.disableDecimal
        int prevCarry = registers.status().getCarry() ? 1 : 0;

        int a = registers.a().getAsInt();
        int aSign = a >> 7;

        int dataVal = sint(data);
        int dataSign = dataVal >> 7;

        int result = a + dataVal + prevCarry;
        int byteResult = result & 0xFF;

        int resultSign = byteResult >> 7;

        boolean overflow = resultSign != aSign && resultSign != dataSign;

        registers.a().setAsByte(byteResult);
        registers.status()
                .setCarry(result > 0xFF)
                .setZero(byteResult == 0)
                .setOverflow(overflow || setOverflow.isActive())
                .setNegative(resultSign == 1);
    }

    public void subtractWithBorrow(@Unsigned byte data) {
        int prevBorrow = registers.status().getBorrow() ? 1 : 0;

        int a = registers.a().getAsInt();
        int aSign = a >> 7;

        int dataVal = sint(data);
        int dataSign = dataVal >> 7;

        int result = a - dataVal - prevBorrow;
        int byteResult = result & 0xFF;

        int resultSign = byteResult >> 7;
        registers.a().setAsByte(byteResult);

        boolean overflow = resultSign != aSign && resultSign == dataSign;

        registers.status()
                .setBorrow(result < 0)
                .setZero(byteResult == 0)
                .setOverflow(overflow || setOverflow.isActive())
                .setNegative(resultSign == 1);
    }

    public @Unsigned byte incrementMemory(@Unsigned byte data) {
        return incrementMemory(data, 1);
    }

    public @Unsigned byte decrementMemory(@Unsigned byte data) {
        return incrementMemory(data, -1);
    }

    private @Unsigned byte incrementMemory(@Unsigned byte data, int by) {
        int dataVal = sint(data);

        int result = dataVal + by;

        // TODO: status register gets updated here but memory outside (in readModifyWrite)
        status.maybeZeroOrNegative(result);

        return ubyte(result);
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

        register.setAsByte(result);
        status.maybeZeroOrNegative(result);
    }

    public void bitwiseOp(@Unsigned byte operand, IntBinaryOperator operator) {
        int result = operator.applyAsInt(accumulator.getAsInt(), sint(operand));

        accumulator.setAsByte(result);
        status.maybeZeroOrNegative(result);
    }

    public void bitwiseAnd(@Unsigned byte operand) {
        bitwiseOp(operand, (a, b) -> a & b);
    }

    // TODO: make methods package-private, when CU is here, no 0 arg methods

    void bitwiseOr() {
        bitwiseOp(operand2.getData(), (a, b) -> a | b);
    }

    public void bitwiseXor(@Unsigned byte operand) {
        bitwiseOp(operand, (a, b) -> a ^ b);
    }

    public void bitTest(@Unsigned byte data) {
        @Unsigned byte a = registers.a().get();

        int aVal = sint(a);
        int dataVal = sint(data);

        int result = aVal & dataVal & 0xFF;

        registers.status()
                .setZero(result == 0)
                .setOverflow((dataVal & (1 << 6)) != 0)
                .setNegative((dataVal & (1 << 7)) != 0);
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

        int regVal = sint(reg);
        int dataVal = sint(data);

        int result = regVal - dataVal;

        registers.status()
                .setBorrow(result < 0)
                .maybeZeroOrNegative(result);
    }

    public @Unsigned byte rotateLeft(@Unsigned byte data) {
        int oldCarry = registers.status().getCarry() ? 1 : 0;

        int dataVal = sint(data);

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
        int newData = (sint(data) >> 1) | oldCarry; // modify // FIXME: consider >> vs >>>

        registers.status()
                .setCarry(newCarry)
                .setZero(newData == 0)
                .setNegative((newData & (1 << 7)) > 0);

        return ubyte(newData);
    }

    public @Unsigned byte arithmeticShiftLeft(@Unsigned byte data) {
        int dataVal = sint(data);

        int result = dataVal << 1;
        int resultByte = result & 0xFF;

        registers.status()
                .setCarry(((result & (1 << 8)) > 0))
                .setZero(resultByte == 0)
                .setNegative((resultByte & (1 << 7)) > 0);

        return ubyte(resultByte);
    }

    public @Unsigned byte logicalShiftRight(@Unsigned byte data) {
        int dataVal = sint(data);

        int result = dataVal >> 1;
        int resultByte = result & 0xFF;

        registers.status()
                .setCarry((dataVal & 0b1) > 0)
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
