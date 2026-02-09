package net.novaware.nes.core.cpu;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.register.StatusRegister;
import net.novaware.nes.core.register.AddressRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.RegisterFile;
import net.novaware.nes.core.register.ShortRegister;

import java.util.List;

@BoardScope
public class CpuRegisters extends RegisterFile {

    private final AddressRegister programCounter;

    private final AddressRegister memoryAddress;
    private final DataRegister  memoryData;

    /** @see Instruction#opcode() */
    private final DataRegister currentInstruction;
    private final ShortRegister currentOperand;

    /** @see InstructionGroup#ordinal() TODO: use aaa000cc bits */
    private final DataRegister  decodedInstruction;
    private final DelegatingRegister decodedOperand;

    private final DataRegister accumulator;

    private final DataRegister indexX;
    private final DataRegister indexY;

    private final StatusRegister status;

    private final AddressRegister stackPointer;
    private final AddressRegister stackSegment;

    // TODO: falling edge triggered, not high / low state
    // TODO: consider adding to data/address register list
    private final DataRegister nmi = new ByteRegister("NMI");
    private final DataRegister irq = new ByteRegister("IRQ");
    private final DataRegister reset = new ByteRegister("RST");

    // TODO: add separate irq/nmi latch registers?

    // TODO: segment registers: Code Segment, Video Segment, Extra Segments configured by mappers

    @Inject
    public CpuRegisters() {
        super("CPU");

        dataRegisters = List.of(
            memoryData = new ByteRegister("MDR"),
            currentInstruction = new ByteRegister("CIR"),
            decodedInstruction = new ByteRegister("DIR"),
            accumulator = new ByteRegister("A"),
            indexX = new ByteRegister("X"),
            indexY = new ByteRegister("Y")
        );

        addressRegisters = List.of(
            programCounter = new ShortRegister("PC"),
            memoryAddress = new ShortRegister("MAR"),
            currentOperand = new ShortRegister("COR"),
            stackPointer = new ShortRegister("S"),
            stackSegment = new ShortRegister("SS")
        );

        decodedOperand = new DelegatingRegister("DOR");
        status = new StatusRegister("P");
    }

    public AddressRegister getProgramCounter() {
        return programCounter;
    }

    /** @see #getProgramCounter() */
    public AddressRegister pc() {
        return programCounter;
    }

    public AddressRegister getMemoryAddress() {
        return memoryAddress;
    }

    /** @see #getMemoryAddress() */
    public AddressRegister mar() {
        return memoryAddress;
    }

    public DataRegister getMemoryData() {
        return memoryData;
    }

    /** @see #getMemoryData() */
    public DataRegister mdr() {
        return memoryData;
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

    public DataRegister getDecodedInstruction() {
        return decodedInstruction;
    }

    /** @see #getDecodedInstruction() */
    public DataRegister dir() {
        return decodedInstruction;
    }

    public DelegatingRegister getDecodedOperand() {
        return decodedOperand;
    }

    /** @see #getDecodedOperand() */
    public DelegatingRegister dor() {
        return decodedOperand;
    }

    public DataRegister getAccumulator() {
        return accumulator;
    }

    /** @see #getAccumulator() */
    public DataRegister a() {
        return accumulator;
    }

    public DataRegister getIndexX() {
        return indexX;
    }

    /** @see #getIndexX() */
    public DataRegister x() {
        return indexX;
    }

    public DataRegister getIndexY() {
        return indexY;
    }

    /** @see #getIndexY() */
    public DataRegister y() {
        return indexY;
    }

    public StatusRegister getStatus() {
        return status;
    }

    /** @see #getStatus() */
    public StatusRegister status() {
        return status;
    }

    public AddressRegister getStackPointer() {
        return stackPointer;
    }

    /** @see #getStackPointer() */
    public AddressRegister sp() {
        return stackPointer;
    }

    public AddressRegister getStackSegment() {
        return stackSegment;
    }

    /** @see #getStackSegment() */
    public AddressRegister ss() {
        return stackSegment;
    }
}
