package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_BUS;
import static net.novaware.nes.core.cpu.CpuModule.CPU_CYCLE_COUNTER;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

@BoardScope
public class ControlUnit implements Unit {

    public static final @Unsigned short RESET_VECTOR = ushort(0xFFFC);

    @Used
    private CpuRegisters registers;

    @Used
    CycleCounter cycleCounter;

    @Used
    private MemoryBus memoryBus;

    @Used
    private AddressGen addressGen;

    @Used
    private ArithmeticLogic alu;

    @Inject
    public ControlUnit(
        CpuRegisters registers,
        @Named(CPU_CYCLE_COUNTER) CycleCounter cycleCounter,
        @Named(CPU_BUS) MemoryBus memoryBus,
        AddressGen addressGen,
        ArithmeticLogic alu
    ) {
        this.registers = registers;
        this.cycleCounter = cycleCounter;
        this.memoryBus = memoryBus;
        this.addressGen = addressGen;
        this.alu = alu;
    }

    public void powerOn() {
        registers.accumulator.setAsByte(0);
        registers.indexX.setAsByte(0);
        registers.indexY.setAsByte(0);
        registers.programCounter.set(RESET_VECTOR);
        registers.stackPointer.setAsByte(0);
        registers.status.powerOn();
    }

    @Override
    public void reset() {
        cycleCounter.setValue(4); // stabilizing after takes about n cycles, 6 cycles according to pdf

        registers.programCounter.set(addressGen.fetchAddress(RESET_VECTOR));

        registers.status.reset();

        // TODO: move to stack engine
        int sp = registers.stackPointer.getAsInt();
        sp -= 3;
        registers.stackPointer.setAsByte(sp);
    }

    public void fetch() {
        @Unsigned short currentAddress = registers.programCounter.get();
        int currentAddressInt = uint(currentAddress);

        @Unsigned byte opcode = memoryBus.specifyAnd(currentAddress)
                .readByte();

        // TODO: this needs to be the only point of lookup (data fetcher, address decoder, instruction executor)
        // which is propagated through the pipeline and parts used when needed?
        Instruction instruction = InstructionRegistry.fromOpcode(opcode);
        registers.currentInstruction.set(instruction.opcode());

        int size = instruction.size();

        int newPc = currentAddressInt + size;
        registers.programCounter.setAsShort(newPc);

        switch (size) {
            case 1: // NOTE: covers a dummy read even for single byte instructions
            case 2:
                @Unsigned byte operand = memoryBus.specifyAnd(ushort(currentAddressInt + 1)).readByte();
                registers.currentOperand.set(ushort(operand));
                break;

            case 3:
                @Unsigned byte operandLo = memoryBus.specifyAnd(ushort(currentAddressInt + 1)).readByte();
                @Unsigned byte operandHi = memoryBus.specifyAnd(ushort(currentAddressInt + 2)).readByte();
                registers.currentOperand.set(ushort((uint(operandHi) << 8) | uint(operandLo)));
                break;

            default:
                throw new IllegalStateException("Unexpected instruction size: " + size);
        }

    }

    // TODO: move to decoder
    public void decode() {
        @Unsigned byte opcode = registers.currentInstruction.get();


        Instruction instruction = InstructionRegistry.fromOpcode(opcode);

        // TODO: this won't work. Make it a single switch and be done with it. Or maybe it will?
        registers.decodedInstruction.setAsByte(instruction.group().ordinal());

        AddressingMode addressingMode = instruction.addressingMode();
        @Unsigned short operand = registers.currentOperand.get();

        switch(addressingMode) {
            case IMPLIED:
                registers.decodedOperand.configureEmpty();
                break;
            case IMMEDIATE:
                registers.decodedOperand.configureByte();
                registers.decodedOperand.setData(ubyte(uint(operand) & 0xFF)); // FIXME: ugly
                break;
            case ACCUMULATOR:
                registers.decodedOperand.configureByteRegister(registers.accumulator);
                break;
            case ZERO_PAGE: // 0x00NN
            case ABSOLUTE:  // 0xNNNN
                registers.decodedOperand.configureMemory(memoryBus, operand);
                break;
            case RELATIVE: // only branches
                break;
            case ABSOLUTE_INDIRECT: // only jump
                @Unsigned short address = addressGen.fetchAddress(operand);
                registers.decodedOperand.configureMemory(memoryBus, address);
                break;
            case INDEXED_ZERO_PAGE_X:
            case INDEXED_ZERO_PAGE_Y:
            case INDEXED_ABSOLUTE_X:
            case INDEXED_ABSOLUTE_Y:
            case PRE_INDEXED_INDIRECT_X:
            case POST_INDEXED_INDIRECT_Y:
                break;
            case UNKNOWN:
                throw new UnsupportedOperationException("Unsupported addressing mode: " + addressingMode.name());
        }
    }

    public void execute() {

        // TODO: need a flag or null register value when pipeline is empty

        int instrGroup = registers.decodedInstruction.getAsInt();

        InstructionGroup instruction = InstructionGroup.valueOf(instrGroup);

        switch(instruction) {
            case JUMP_TO:
                @Unsigned short operand = registers.decodedOperand.getAddress();
                registers.programCounter.set(operand);
                break;
            case BITWISE_OR:
                alu.bitwiseOr(registers.decodedOperand.getData());
                break;
            case BITWISE_AND:
                alu.bitwiseAnd(registers.decodedOperand.getData());
                break;
            case ROTATE_LEFT:
                @Unsigned byte data = registers.decodedOperand.getData(); // read
                registers.decodedOperand.setData(data); // write unmodified
                @Unsigned byte newData = alu.rotateLeft(data); // modify
                registers.decodedOperand.setData(newData); // write
                break;
            case NO_OPERATION:
                break;
            default:
                throw new UnsupportedOperationException("Unsupported instruction: " + instruction.name());
        }
        // execute the handler
        // write back to mem / reg
        // fetch the next instruction
    }
}
