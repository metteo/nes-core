package net.novaware.nes.core.cpu;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;

@BoardScope
public class CpuRegisters extends RegisterFile {

    // TODO: getters for registers

    // region Fetch
    public ShortRegister programCounter = new ShortRegister("PC"); // NOTE: PCL + PCH

    // TODO: part of control unit
    /** @see Instruction#opcode() */
    public ByteRegister  currentInstruction = new ByteRegister("CIR");
    public ShortRegister currentOperand = new ShortRegister("COR");

    // endregion
    // region Decode

    // TODO: part of Instruction Decoder?
    /** @see InstructionGroup#ordinal() */
    public ByteRegister  decodedInstruction = new ByteRegister("DIR");
    public DelegatingRegister decodedOperand = new DelegatingRegister("DOR");

    // endregion

    public ByteRegister accumulator = new ByteRegister("A");

    public ByteRegister indexX = new ByteRegister("X");
    public ByteRegister indexY = new ByteRegister("Y");

    public StatusRegister status = new StatusRegister("P");

    public ByteRegister stackPointer = new ByteRegister("S");

    // TODO: segment registers: Code Segment, Data Segment, Extra Segment, Stack Segment

    // quasi pipeline
    // IF -> ID -> EX
    //             IF -> ID -> EX

    @Inject
    public CpuRegisters() {

    }

}
