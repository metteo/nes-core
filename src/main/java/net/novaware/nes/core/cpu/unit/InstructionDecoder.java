package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.register.InstructionRegister;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class InstructionDecoder implements Unit {

    @Used private final CpuRegFile registers;

    @Used private final ByteRegister currentInstruction;
    @Used private final ShortRegister currentOperand;
    @Used private final InstructionRegister decodedInstruction;
    @Used private final DelegatingRegister decodedOperand;

    @Used private final CycleCounter cycleCounter;
    @Used private final MemoryBus memoryBus;
    @Used private final AddressGen addressGen;

    @Inject
    public InstructionDecoder(
        CpuRegFile registers,

        @CpuVar(CI) ByteRegister currentInstruction,
        @CpuVar(CO) ShortRegister currentOperand,

        @CpuVar(DI) InstructionRegister decodedInstruction,
        @CpuVar(DO) DelegatingRegister decodedOperand,

        @CpuVar(CC) CycleCounter cycleCounter,
        @CpuVar(BUS) MemoryBus memoryBus,
        AddressGen addressGen
    ) {
        this.registers = registers;

        this.currentInstruction = currentInstruction;
        this.currentOperand = currentOperand;
        this.decodedInstruction = decodedInstruction;
        this.decodedOperand = decodedOperand;

        this.cycleCounter = cycleCounter;
        this.memoryBus = memoryBus;
        this.addressGen = addressGen;
    }

    public void decode() {
        @Unsigned byte opcode = currentInstruction.get();

        Instruction instruction = InstructionRegistry.fromOpcode(opcode);
        decodedInstruction.set(instruction);

        AddressingMode addressingMode = instruction.addressingMode();
        @Unsigned short operand = currentOperand.get();

        switch (addressingMode) {
            case IMPLIED -> decodeImplied();
            case ACCUMULATOR -> decodeAccumulator();

            case IMMEDIATE -> decodeImmediate(operand);

            case RELATIVE -> decodeRelative(operand);

            case ZERO_PAGE -> decodeZeroPage(operand);

            case ZERO_PAGE_X -> decodeZeroPageIndexed(registers.x(), operand);
            case ZERO_PAGE_Y -> decodeZeroPageIndexed(registers.y(), operand);

            case ZERO_PAGE_X_INDIRECT -> decodeZeroPageIndexed_X_Indirect(operand);

            case ZERO_PAGE_INDIRECT_Y_R -> decodeZeroPageIndexed_Y_IndirectRead(operand);
            case ZERO_PAGE_INDIRECT_Y_W -> decodeZeroPageIndexed_Y_IndirectWrite(operand);

            case ABSOLUTE -> decodeAbsolute(operand);

            case ABSOLUTE_X_R -> decodeAbsoluteIndexedRead(registers.x(), operand);
            case ABSOLUTE_X_W -> decodeAbsoluteIndexedWrite(registers.x(), operand);

            case ABSOLUTE_Y_R -> decodeAbsoluteIndexedRead(registers.y(), operand);
            case ABSOLUTE_Y_W -> decodeAbsoluteIndexedWrite(registers.y(), operand);

            case ABSOLUTE_INDIRECT -> decodeAbsoluteIndirect(operand); // only jump

            case UNKNOWN -> throw new UnsupportedOperationException("Unsupported opcode: " + Hex.s(opcode));
        }
    }

    private void decodeZeroPageIndexed_Y_IndirectRead(@Unsigned short operand) {
        int address = sint(addressGen.buggyFetchAddress(operand)); // stay within zero page
        int yVal = registers.y().getAsInt();

        int result = address + yVal;

        boolean pageChange = (address & 0xFF00) != (result & 0xFF00);
        cycleCounter.maybeIncrement(pageChange); // TODO: this should be a bus read from address without a zero page wrap (oops)

        decodedOperand.configureMemory(memoryBus, ushort(result));
    }

    private void decodeZeroPageIndexed_Y_IndirectWrite(@Unsigned short operand) {
        int address = sint(addressGen.buggyFetchAddress(operand)); // stay within zero page
        int yVal = registers.y().getAsInt();

        int result = address + yVal;

        cycleCounter.increment(); // TODO: this should be a bus read from address without a zero page wrap (oops)

        decodedOperand.configureMemory(memoryBus, ushort(result));
    }

    private void decodeZeroPageIndexed_X_Indirect(@Unsigned short operand) {
        int address = sint(operand);
        int xVal = registers.x().getAsInt();

        memoryBus.access(operand).read().data(); // internal cycle wasted for addition below
        int indirectAddress = address + xVal;

        @Unsigned short result = addressGen.buggyFetchAddress(ushort(indirectAddress & 0xFF)); // stay within zero page
        decodedOperand.configureMemory(memoryBus, result);
    }

    private void decodeAbsoluteIndexedRead(DataRegister indexRegister, @Unsigned short operand) {
        int indexVal = indexRegister.getAsInt();

        int result = indexVal + sint(operand);

        boolean pageChange = (sint(operand) & 0xFF00) != (result & 0xFF00);
        cycleCounter.maybeIncrement(pageChange);

        decodedOperand.configureMemory(memoryBus, ushort(result));
    }

    private void decodeAbsoluteIndexedWrite(DataRegister indexRegister, @Unsigned short operand) {
        int indexVal = indexRegister.getAsInt();

        int result = indexVal + sint(operand);

        cycleCounter.increment(); // FIXME: always increment on write, change into proper bus op

        decodedOperand.configureMemory(memoryBus, ushort(result));
    }

    private void decodeZeroPageIndexed(DataRegister indexRegister, @Unsigned short operand) {
        int indexVal = indexRegister.getAsInt();

        memoryBus.access(operand).read().data(); // internal cycle wasted for addition below
        int result = (indexVal + sint(operand)) & 0xFF;

        decodedOperand.configureMemory(memoryBus, ushort(result));
    }

    private void decodeRelative(@Unsigned short operand) {
        @SuppressWarnings("signedness")
        @Signed int signedOperand = (byte) operand;
        int pc = registers.pc().getAsInt();

        decodedOperand.configureAddress(ushort(pc + signedOperand));
    }

    private void decodeAbsoluteIndirect(@Unsigned short operand) {
        @Unsigned short address = addressGen.buggyFetchAddress(operand);
        decodedOperand.configureMemory(memoryBus, address);
    }

    private void decodeAbsolute(@Unsigned short operand) {
        decodedOperand.configureMemory(memoryBus, operand);
    }

    private void decodeZeroPage(@Unsigned short operand) {
        decodedOperand.configureMemory(memoryBus, operand);
    }

    private void decodeAccumulator() {
        decodedOperand.configureDataRegister(registers.a());
    }

    private void decodeImmediate(@Unsigned short operand) {
        int data = sint(operand);
        decodedOperand.configureData(ubyte(data));
    }

    private void decodeImplied() {
        decodedOperand.configureEmpty();
    }
}
