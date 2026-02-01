package net.novaware.nes.core.cpu;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.cpu.register.CpuRegisterFile;
import net.novaware.nes.core.cpu.unit.ArithmeticLogic;
import net.novaware.nes.core.memory.SystemBus;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

/**
 * https://web.archive.org/web/20221112231348if_/http://archive.6502.org/datasheets/rockwell_r650x_r651x.pdf
 */
@BoardScope
public class Cpu {

    public static final @Unsigned short RESET_VECTOR = ushort(0xFFFC);

    @Owned
    private CpuRegisterFile registers;

    // TODO: control unit

    @Used
    private SystemBus systemBus; // call through a delegating wrapper that counts the cycles?

    @Owned
    private ArithmeticLogic alu;

    @Inject
    public Cpu(
        CpuRegisterFile registers,
        SystemBus systemBus,
        ArithmeticLogic alu
    ) {
        this.registers = registers;
        this.systemBus = systemBus;
        this.alu = alu;
    }

    public void initialize() {
        alu.initialize();
    }

    public void powerOn() {
        registers.accumulator.setAsByte(0);
        registers.indexX.setAsByte(0);
        registers.indexY.setAsByte(0);
        registers.programCounter.set(RESET_VECTOR);
        registers.stackPointer.setAsByte(0);
        registers.status.powerOn();
    }

    public void reset() { // TODO:
        registers.cycleCounter.setValue(7); // stabilizing after takes about n cycles, 6 cycles according to pdf

        registers.programCounter.set(fetchAddress(RESET_VECTOR));

        registers.status.reset();

        int sp = registers.stackPointer.getAsInt();
        sp -= 3;
        registers.stackPointer.setAsByte(sp);

        alu.reset();
    }

    private @Unsigned short fetchAddress(@Unsigned short address) {
        @Unsigned byte addrLo = systemBus.specifyAnd(address).readByte();
        @Unsigned byte addrHi = systemBus.specifyAnd(ushort(uint(address) + 1)).readByte();
        return ushort(uint(addrHi) << 8 | uint(addrLo));
    }

    public void fetch() {
        @Unsigned short pc = registers.programCounter.get();

        registers.memoryAddress.set(pc);

        @Unsigned byte opcode = systemBus.specifyAnd(pc)
                .readByte();

        registers.memoryData.set(opcode);

        Instruction instruction = InstructionRegistry.fromOpcode(opcode);
        registers.currentInstruction.set(instruction.opcode());

        int size = instruction.size();

        int newPc = uint(pc) + size;
        registers.programCounter.setAsShort(newPc);

        // TODO: MAR & MDR should be updated every time bus is accessed
        int memAddr = registers.memoryAddress.getAsInt();

        switch (size) {
            case 1: // NOTE: covers a dummy read even for single byte instructions (or do we do it in execute)
            case 2:
                @Unsigned byte operand = systemBus.specifyAnd(ushort(memAddr + 1)).readByte();
                registers.currentOperand.set(ushort(operand));
                break;

            case 3:
                @Unsigned byte operandLo = systemBus.specifyAnd(ushort(memAddr + 1)).readByte();
                @Unsigned byte operandHi = systemBus.specifyAnd(ushort(memAddr + 2)).readByte();
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

        registers.decodedInstruction.setAsByte(instruction.group().ordinal());

        AddressingMode addressingMode = instruction.addressingMode();
        @Unsigned short operand = registers.currentOperand.get();

        @Unsigned short address = switch(addressingMode) {
            case ABSOLUTE -> operand;
            case ABSOLUTE_INDIRECT -> fetchAddress(operand);
            default -> throw new UnsupportedOperationException("Unsupported addressing mode: " + addressingMode.name());
        };

        registers.decodedOperand.set(address);
    }



    public void execute() {

        // TODO: need a flag or null register value when pipeline is empty

        int instrGroup = registers.decodedInstruction.getAsInt();
        @Unsigned short operand = registers.decodedOperand.get();

        InstructionGroup instruction = InstructionGroup.valueOf(instrGroup);

        switch(instruction) {
            case JUMP_TO:
                registers.programCounter.set(operand);
                break;
            case BITWISE_OR:
                @Unsigned byte memVal = systemBus.specifyAnd(operand).readByte();
                alu.bitwiseOr(memVal);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported instruction: " + instruction.name());
        }
        // execute the handler
        // write back to mem / reg
        // fetch the next instruction
    }

    public void ready() {
        // NOTE: input signal that allows to halt or single cycle the processor
    }

    public void cycle() {
        execute();

        fetch();
        decode();
    }
}
