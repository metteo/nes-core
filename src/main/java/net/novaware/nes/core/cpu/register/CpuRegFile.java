package net.novaware.nes.core.cpu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;

import java.util.List;

import static net.novaware.nes.core.cpu.inject.CpuVarName.A;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SP;
import static net.novaware.nes.core.cpu.inject.CpuVarName.X;
import static net.novaware.nes.core.cpu.inject.CpuVarName.Y;

/**
 * CPU Standard Registers
 */
@BoardScope
public class CpuRegFile extends RegisterFile {

    private final ShortRegister programCounter;

    private final ByteRegister accumulator;

    private final ByteRegister indexX;
    private final ByteRegister indexY;

    private final ByteRegister stackPointer;

    private final StatusRegister status;

    @Inject
    public CpuRegFile(
        @CpuVar(PC) ShortRegister programCounter,
        @CpuVar(A) ByteRegister accumulator,
        @CpuVar(X) ByteRegister indexX,
        @CpuVar(Y) ByteRegister indexY,
        @CpuVar(SP) ByteRegister stackPointer,
        @CpuVar(PS) StatusRegister status
    ) {
        super("CPU_REG");

        dataRegisters = List.of(
            this.accumulator = accumulator,
            this.indexX = indexX,
            this.indexY = indexY,
            this.stackPointer = stackPointer
        );

        addressRegisters = List.of(
            this.programCounter = programCounter
        );

        this.status = status;
    }

    public ShortRegister getProgramCounter() {
        return programCounter;
    }

    /** @see #getProgramCounter() */
    public ShortRegister pc() {
        return programCounter;
    }

    public ByteRegister getAccumulator() {
        return accumulator;
    }

    /** @see #getAccumulator() */
    public ByteRegister a() {
        return accumulator;
    }

    public ByteRegister getIndexX() {
        return indexX;
    }

    /** @see #getIndexX() */
    public ByteRegister x() {
        return indexX;
    }

    public ByteRegister getIndexY() {
        return indexY;
    }

    /** @see #getIndexY() */
    public ByteRegister y() {
        return indexY;
    }

    public StatusRegister getStatus() {
        return status;
    }

    /** @see #getStatus() */
    public StatusRegister status() {
        return status;
    }

    public ByteRegister getStackPointer() {
        return stackPointer;
    }

    /** @see #getStackPointer() */
    public ByteRegister sp() {
        return stackPointer;
    }
}
