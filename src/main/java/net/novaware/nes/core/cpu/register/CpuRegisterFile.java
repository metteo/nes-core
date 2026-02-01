package net.novaware.nes.core.cpu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.ShortRegister;

@BoardScope
public class CpuRegisterFile {

    // TODO: cycle counter should have own register file (for cpu, ppu and apu
    public CycleCounter cycleCounter = new CycleCounter("CPUCC");

    // region Fetch
    public ShortRegister programCounter = new ShortRegister("PC"); // NOTE: PCL + PCH

    // TODO: this belongs to memory controller / memory bus as currentAddress variable / currentData variable for open bus
    public ShortRegister memoryAddress = new ShortRegister("MAR");
    public ByteRegister  memoryData = new ByteRegister("MDR");

    // TODO: possibly part of control unit?
    /** @see Instruction#opcode() */
    public ByteRegister  currentInstruction = new ByteRegister("CIR");
    public ShortRegister currentOperand = new ShortRegister("COR");

    // endregion
    // region Decode

    // TODO: part of Instruction Decoder?
    /** @see InstructionGroup#ordinal() */
    public ByteRegister  decodedInstruction = new ByteRegister("DIR");
    /** @see AddressingMode#ABSOLUTE */
    public ShortRegister decodedOperand = new ShortRegister("DOR");

    // endregion

    public ByteRegister accumulator = new ByteRegister("A");

    public ByteRegister indexX = new ByteRegister("X");
    public ByteRegister indexY = new ByteRegister("Y");

    public ProcessorRegister status = new ProcessorRegister("P");

    public ByteRegister stackPointer = new ByteRegister("S");

    // quasi pipeline
    // IF -> ID -> EX
    //             IF -> ID -> EX

    @Inject
    public CpuRegisterFile() {

    }

}
