package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_CYCLE_COUNTER;
import static net.novaware.nes.core.cpu.memory.MemoryModule.CPU_BUS;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class InstructionDecoder implements Unit {

    @Used private final CpuRegisters registers;
    @Used private final CycleCounter cycleCounter;
    @Used private final MemoryBus memoryBus;
    @Used private final AddressGen addressGen;

    @Inject
    public InstructionDecoder(
            CpuRegisters registers,
            @Named(CPU_CYCLE_COUNTER)CycleCounter cycleCounter,
            @Named(CPU_BUS) MemoryBus memoryBus,
            AddressGen addressGen
    ) {
        this.registers = registers;
        this.cycleCounter = cycleCounter;
        this.memoryBus = memoryBus;
        this.addressGen = addressGen;
    }

    public void decode() {
        @Unsigned byte opcode = registers.cir().get();

        Instruction instruction = InstructionRegistry.fromOpcode(opcode);

        // TODO: Hide log generation behind a flag. sout is sloooow
        System.out.println(" " + instruction.group().mnemonic() + "                            "
                + " A:" + Hex.s(registers.a().get()).toUpperCase()
                + " X:" + Hex.s(registers.x().get()).toUpperCase()
                + " Y:" + Hex.s(registers.y().get()).toUpperCase()
                + " P:" + Hex.s(registers.status().get().get()).toUpperCase()
                + " SP:" + Hex.s(registers.sp().get()).toUpperCase()
//                + " PPU:       "
//                + " CYC:" + cycleCounter.getValue()
        );

        // TODO: this won't work. Make it a single switch and be done with it. Or maybe it will?
        registers.dir().setAsByte(instruction.group().ordinal());

        AddressingMode addressingMode = instruction.addressingMode();
        @Unsigned short operand = registers.cor().get();

        switch (addressingMode) {
            case IMPLIED -> decodeImplied();
            case IMMEDIATE -> decodeImmediate(operand);
            case ACCUMULATOR -> decodeAccumulator();
            case ZERO_PAGE, ABSOLUTE -> decodeAbsolute(operand); // 0x00NN or 0xNNNN
            case ABSOLUTE_INDIRECT -> decodeAbsoluteIndirect(operand); // only jump
            case RELATIVE -> decodeRelative(operand); // only branches
            case INDEXED_ZERO_PAGE_X -> decodeIndexedZeroPage(registers.x(), operand);
            case INDEXED_ZERO_PAGE_Y -> decodeIndexedZeroPage(registers.y(), operand);
            case INDEXED_ABSOLUTE_X -> decodeIndexedAbsolute(registers.x(), operand);
            case INDEXED_ABSOLUTE_Y -> decodeIndexedAbsolute(registers.y(), operand);
            case PRE_INDEXED_INDIRECT_X -> decodePreIndexedIndirectX(operand);
            case POST_INDEXED_INDIRECT_Y -> decodePostIndexedIndirectY(operand);
            case UNKNOWN -> throw new UnsupportedOperationException("Unsupported opcode: " + Hex.s(opcode));
        }
    }

    private void decodePostIndexedIndirectY(@Unsigned short operand) {
        int address = sint(addressGen.fetchAddress(operand));
        int yVal = registers.y().getAsInt();

        int result = address + yVal;

        boolean pageChange = (operand & 0xFF) == 0xFF;
        cycleCounter.maybeIncrement(pageChange);

        registers.dor().configureMemory(memoryBus, ushort(result));
    }

    private void decodePreIndexedIndirectX(@Unsigned short operand) {
        int address = sint(operand);
        int xVal = registers.x().getAsInt();

        int indirectAddress = address + xVal;

        // FIXME: it is supposed to be without carry for this and fetchAddress?
        @Unsigned short result = addressGen.fetchAddress(ushort(indirectAddress & 0xFF));
        registers.dor().configureMemory(memoryBus, result);
    }

    private void decodeIndexedAbsolute(DataRegister indexRegister, @Unsigned short operand) {
        int indexVal = indexRegister.getAsInt();

        int result = indexVal + sint(operand);

        boolean pageChange = (sint(operand) & 0xFF00) != (result & 0xFF00);
        cycleCounter.maybeIncrement(pageChange);

        this.registers.dor().configureMemory(memoryBus, ushort(result));
    }

    private void decodeIndexedZeroPage(DataRegister indexRegister, @Unsigned short operand) {
        int indexVal = indexRegister.getAsInt();

        int result = (indexVal + sint(operand)) & 0xFF;

        this.registers.dor().configureMemory(memoryBus, ushort(result));
    }

    private void decodeRelative(@Unsigned short operand) {
        @SuppressWarnings("signedness")
        @Signed int signedOperand = (byte) operand;
        int pc = registers.pc().getAsInt();

        registers.dor().configureAddress(ushort(pc + signedOperand));
    }

    private void decodeAbsoluteIndirect(@Unsigned short operand) {
        @Unsigned short address = addressGen.buggyFetchAddress(operand);
        registers.dor().configureMemory(memoryBus, address);
    }

    private void decodeAbsolute(@Unsigned short operand) {
        registers.dor().configureMemory(memoryBus, operand);
    }

    private void decodeAccumulator() {
        registers.dor().configureDataRegister(registers.a());
    }

    private void decodeImmediate(@Unsigned short operand) {
        int data = sint(operand);
        registers.dor().configureData(ubyte(data));
    }

    private void decodeImplied() {
        registers.dor().configureEmpty();
    }
}
