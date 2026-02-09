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
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_BUS;
import static net.novaware.nes.core.cpu.CpuModule.CPU_CYCLE_COUNTER;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

@BoardScope
public class ControlUnit implements Unit {

    public static final @Unsigned short RESET_VECTOR = ushort(0xFFFC); // TODO: move to InterruptLogic

    @Used private final CpuRegisters registers;
    @Used private final CycleCounter cycleCounter;
    @Used private final MemoryBus memoryBus;
    @Used private final AddressGen addressGen;
    @Used private final ArithmeticLogic alu;
    @Used private final MemoryMgmt mmu;

    @Inject
    public ControlUnit(
        CpuRegisters registers,
        @Named(CPU_CYCLE_COUNTER) CycleCounter cycleCounter,
        @Named(CPU_BUS) MemoryBus memoryBus,
        AddressGen addressGen,
        ArithmeticLogic alu,
        MemoryMgmt mmu
    ) {
        this.registers = registers;
        this.cycleCounter = cycleCounter;
        this.memoryBus = memoryBus;
        this.addressGen = addressGen;
        this.alu = alu;
        this.mmu = mmu;
    }

    @Override
    public void initialize() {
        cycleCounter.setValue(3); // stabilizing after takes about n cycles, 6 cycles according to pdf

        registers.a().setAsByte(0);
        registers.x().setAsByte(0);
        registers.y().setAsByte(0);
        registers.sp().highAsByte(0x01).lowAsByte(0xFD);
        registers.status().initialize();

        registers.pc().set(addressGen.fetchAddress(RESET_VECTOR));
        fetchOpcode();
    }

    @Override
    public void reset() {
        cycleCounter.setValue(3); // stabilizing after takes about n cycles, 6 cycles according to pdf

        registers.pc().set(addressGen.fetchAddress(RESET_VECTOR));
        fetchOpcode();

        registers.status().reset();

        // TODO: move to stack engine
        int sp = registers.sp().lowAsInt();
        sp -= 3;
        registers.sp().lowAsByte(sp);
    }

    /**
     * Last cycle of the instruction
     */
    public void fetchOpcode() {
        @Unsigned short opcodeAddress = addressGen.getPc();
        @Unsigned byte opcode = mmu.specifyAnd(opcodeAddress).readByte();

        registers.cir().set(opcode);
    }

    public void fetchOperandLo() {
        @Unsigned short operandLoAddress = addressGen.getPc();
        @Unsigned byte operandLo = mmu.specifyAnd(operandLoAddress).readByte();

        registers.cor().low(operandLo);
    }

    public void fetchOperandHi() {
        int size = InstructionRegistry.fromOpcode(registers.cir().get()).size();

        if (size < 3) { // TODO: maybe read the size from dedicated array and get rid of the if?
            registers.cor().highAsByte(0x00);
            return;
        }

        @Unsigned short operandHiAddress = addressGen.getPc();
        @Unsigned byte operandHi = mmu.specifyAnd(operandHiAddress).readByte();

        registers.cor().high(operandHi);
    }

    public void fetchOperand() {
        fetchOperandLo();
        fetchOperandHi();
    }

    // TODO: move to decoder
    public void decode() {
        @Unsigned byte opcode = registers.cir().get();

        Instruction instruction = InstructionRegistry.fromOpcode(opcode);

        // TODO: this won't work. Make it a single switch and be done with it. Or maybe it will?
        registers.dir().setAsByte(instruction.group().ordinal());

        AddressingMode addressingMode = instruction.addressingMode();
        @Unsigned short operand = registers.cor().get();

        switch(addressingMode) {
            case IMPLIED:
                registers.dor().configureEmpty();
                break;
            case IMMEDIATE:
                registers.dor().configureByte();
                registers.dor().setData(ubyte(uint(operand) & 0xFF)); // FIXME: ugly
                break;
            case ACCUMULATOR:
                registers.dor().configureByteRegister(registers.a());
                break;
            case ZERO_PAGE: // 0x00NN
                // fall through
            case ABSOLUTE:  // 0xNNNN
                registers.dor().configureMemory(memoryBus, operand);
                break;
            case ABSOLUTE_INDIRECT: // only jump
                @Unsigned short address = addressGen.buggyFetchAddress(operand);
                registers.dor().configureMemory(memoryBus, address);
                break;
            case RELATIVE: // only branches
                @SuppressWarnings("signedness")
                @Signed int signedOperand = operand; // operand is signed
                int pc = registers.pc().getAsInt();
                registers.dor().configureMemory(memoryBus, ushort(pc - instruction.size() + signedOperand));
                break;
            case INDEXED_ZERO_PAGE_X:
                int xxxVal = registers.x().getAsInt();
                int effAddr2 = (xxxVal + uint(operand)) & 0xFF;
                registers.dor().configureMemory(memoryBus, ushort(effAddr2));
                break;
            case INDEXED_ZERO_PAGE_Y:
                int yyyVal = registers.y().getAsInt();
                int effAddr3 = (yyyVal + uint(operand)) & 0xFF;
                registers.dor().configureMemory(memoryBus, ushort(effAddr3));
                break;
            case INDEXED_ABSOLUTE_X:
                int xVal = registers.x().getAsInt();
                int effAddr = xVal + uint(operand);
                boolean pageChange = uint(operand) >> 2 != effAddr >> 2;
                if (pageChange) cycleCounter.increment(); // TODO: figure out ifless way
                registers.dor().configureMemory(memoryBus, ushort(effAddr));
                break;
            case INDEXED_ABSOLUTE_Y: // FIXME: x & y are the same
                int yVal = registers.y().getAsInt();
                int effAddry = yVal + uint(operand);
                boolean pageChangeY = uint(operand) >> 2 != effAddry >> 2;
                if (pageChangeY) cycleCounter.increment(); // TODO: figure out ifless way
                registers.dor().configureMemory(memoryBus, ushort(effAddry));
                break;
            case PRE_INDEXED_INDIRECT_X:
                int xxVal = registers.x().getAsInt();
                // FIXME: it is supposed to be without carry for this and fetchAddress?
                short address1 = addressGen.fetchAddress(ushort((xxVal + uint(operand)) & 0xFF));
                registers.dor().configureMemory(memoryBus, address1);
                break;
            case POST_INDEXED_INDIRECT_Y:
                short address2 = addressGen.fetchAddress(operand);
                int baseAddr = uint(address2);
                int yyVal = baseAddr + registers.y().getAsInt();
                boolean pageChangeYY = (baseAddr >> 2) != (yyVal >> 2);
                if (pageChangeYY) cycleCounter.increment(); // TODO: figure out ifless way
                registers.dor().configureMemory(memoryBus, ushort(yyVal));
                break;
            case UNKNOWN:
                throw new UnsupportedOperationException("Unsupported addressing mode: " + addressingMode.name());
        }
    }

    public void execute() {

        // TODO: need a flag or null register value when pipeline is empty

        int instrGroup = registers.dir().getAsInt();

        InstructionGroup instruction = InstructionGroup.valueOf(instrGroup);

        switch(instruction) {
            case JUMP_TO:
                @Unsigned short operand = registers.dor().getAddress();
                registers.pc().set(operand);
                break;
            case BITWISE_OR:
                alu.bitwiseOr(registers.dor().getData());
                break;
            case BITWISE_AND:
                alu.bitwiseAnd(registers.dor().getData());
                break;
            case ROTATE_LEFT:
                @Unsigned byte data = registers.dor().getData(); // read
                registers.dor().setData(data); // write unmodified
                @Unsigned byte newData = alu.rotateLeft(data); // modify
                registers.dor().setData(newData); // write
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
