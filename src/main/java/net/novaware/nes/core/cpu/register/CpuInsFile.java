package net.novaware.nes.core.cpu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.inject.CpuVarName;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.RegisterFile;

import java.util.List;

/**
 * CPU Instruction / Operand Registers
 */
public class CpuInsFile extends RegisterFile {

    /** @see Instruction#opcode() */
    private final ByteRegister currentInstruction;
    private final ShortRegister currentOperand;

    private final InstructionRegister  decodedInstruction;
    private final DelegatingRegister decodedOperand;

    @Inject
    protected CpuInsFile(
        @CpuVar(CpuVarName.CI) ByteRegister currentInstruction,
        @CpuVar(CpuVarName.CO) ShortRegister currentOperand,

        @CpuVar(CpuVarName.DI) InstructionRegister decodedInstruction,
        @CpuVar(CpuVarName.DO) DelegatingRegister decodedOperand
    ) {
        super("CPU.INS");

        byteRegisters = List.of(
            this.currentInstruction = currentInstruction
        );

        shortRegisters = List.of(
            this.currentOperand = currentOperand
        );

        this.decodedInstruction = decodedInstruction;
        this.decodedOperand = decodedOperand;
    }

    public ByteRegister getCurrentInstruction() {
        return currentInstruction;
    }

    /** @see #getCurrentInstruction() */
    public ByteRegister cir() {
        return currentInstruction;
    }

    public ShortRegister getCurrentOperand() {
        return currentOperand;
    }

    /** @see #getCurrentOperand() */
    public ShortRegister cor() {
        return currentOperand;
    }

    public InstructionRegister getDecodedInstruction() {
        return decodedInstruction;
    }

    /** @see #getDecodedInstruction() */
    public InstructionRegister dir() {
        return decodedInstruction;
    }

    public DelegatingRegister getDecodedOperand() {
        return decodedOperand;
    }

    /** @see #getDecodedOperand() */
    public DelegatingRegister dor() {
        return decodedOperand;
    }
}
