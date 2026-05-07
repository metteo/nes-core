package net.novaware.nes.core.cpu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.inject.CpuVarName;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;

import java.util.List;

/**
 * CPU Instruction / Operand Registers
 */
public class CpuInsFile extends RegisterFile {

    /** @see Instruction#opcode() */
    private final DataRegister currentInstruction;
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

        dataRegisters = List.of(
            this.currentInstruction = currentInstruction
        );

        addressRegisters = List.of(
            this.currentOperand = currentOperand
        );

        this.decodedInstruction = decodedInstruction;
        this.decodedOperand = decodedOperand;
    }

    public DataRegister getCurrentInstruction() {
        return currentInstruction;
    }

    /** @see #getCurrentInstruction() */
    public DataRegister cir() {
        return currentInstruction;
    }

    public AddressRegister getCurrentOperand() {
        return currentOperand;
    }

    /** @see #getCurrentOperand() */
    public AddressRegister cor() {
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
