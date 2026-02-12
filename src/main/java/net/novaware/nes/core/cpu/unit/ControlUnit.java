package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.cpu.unit.ArithmeticLogic.ByteUnaryOperator;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_BUS;
import static net.novaware.nes.core.cpu.CpuModule.CPU_CYCLE_COUNTER;
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
    @Used private final InstructionDecoder decoder;

    @Inject
    public ControlUnit(
        CpuRegisters registers,
        @Named(CPU_CYCLE_COUNTER) CycleCounter cycleCounter,
        @Named(CPU_BUS) MemoryBus memoryBus,
        AddressGen addressGen,
        ArithmeticLogic alu,
        MemoryMgmt mmu,
        InstructionDecoder decoder
    ) {
        this.registers = registers;
        this.cycleCounter = cycleCounter;
        this.memoryBus = memoryBus;
        this.addressGen = addressGen;
        this.alu = alu;
        this.mmu = mmu;
        this.decoder = decoder;
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

    public void decode() {
        decoder.decode();
    }

    public void execute() {
        int instrGroup = registers.dir().getAsInt();

        InstructionGroup instruction = InstructionGroup.valueOf(instrGroup);

        switch (instruction) {
            case ADD_WITH_CARRY      -> alu.addWithCarry(registers.dor().getData());
            case SUBTRACT_WITH_CARRY -> alu.subtractWithBorrow(registers.dor().getData());

            case INCREMENT_MEMORY -> readModifyWrite(alu::incrementMemory);
            case DECREMENT_MEMORY -> readModifyWrite(alu::decrementMemory);

            case INCREMENT_X -> alu.incrementX();
            case DECREMENT_X -> alu.decrementX();

            case INCREMENT_Y -> alu.incrementY();
            case DECREMENT_Y -> alu.decrementY();

            case BRANCH_IF_PLUS         -> branchIf(!registers.status().isNegative());
            case BRANCH_IF_MINUS        -> branchIf(registers.status().isNegative());

            case BRANCH_IF_EQUAL        -> branchIf(registers.status().isZero());
            case BRANCH_IF_NOT_EQUAL    -> branchIf(!registers.status().isZero());

            case BRANCH_IF_CARRY_SET    -> branchIf(registers.status().getCarry());
            case BRANCH_IF_CARRY_CLR    -> branchIf(!registers.status().getCarry());

            case BRANCH_IF_OVERFLOW_SET -> branchIf(registers.status().isOverflow());
            case BRANCH_IF_OVERFLOW_CLR -> branchIf(!registers.status().isOverflow());

            case COMPARE_A -> alu.compareA();
            case COMPARE_X -> alu.compareX();
            case COMPARE_Y -> alu.compareY();

            case JUMP_TO -> registers.pc().set(registers.dor().getAddress());

            case JUMP_TO_SUBROUTINE -> {}
            case RETURN_FROM_SUBROUTINE -> {}

            case SET_CARRY -> registers.status().setCarry(true);
            case CLR_CARRY -> registers.status().setCarry(false);

            case SET_DECIMAL -> registers.status().setDecimal(true);
            case CLR_DECIMAL -> registers.status().setDecimal(false);

            case SET_INTERRUPT_DISABLE -> registers.status().setIrqDisabled(true);
            case CLR_INTERRUPT_DISABLE -> registers.status().setIrqDisabled(false);

            case CLR_OVERFLOW -> registers.status().setOverflow(false);

            case BREAK -> {}
            case RETURN_FROM_INTERRUPT -> {}

            case BITWISE_AND -> alu.bitwiseAnd(registers.dor().getData());
            case BITWISE_OR -> alu.bitwiseOr(registers.dor().getData());
            case BITWISE_XOR -> {}
            case BIT_TEST -> {}

            case LOAD_A_WITH_MEMORY -> {}
            case STORE_A_IN_MEMORY -> {}

            case LOAD_X_WITH_MEMORY -> {}
            case STORE_X_IN_MEMORY -> {}

            case LOAD_Y_WITH_MEMORY -> {}
            case STORE_Y_IN_MEMORY -> {}

            case NO_OPERATION -> {}

            case TRANSFER_A_TO_X -> {}
            case TRANSFER_X_TO_A -> {}

            case TRANSFER_A_TO_Y -> {}
            case TRANSFER_Y_TO_A -> {}

            case ARITHMETIC_SHIFT_LEFT -> readModifyWrite(alu::arithmeticShiftLeft);
            case LOGICAL_SHIFT_RIGHT -> readModifyWrite(alu::logicalShiftRight);

            case ROTATE_LEFT  -> readModifyWrite(alu::rotateLeft);
            case ROTATE_RIGHT -> readModifyWrite(alu::rotateRight);

            case PUSH_A -> {}
            case PULL_A -> {}

            case PUSH_PROC_STATUS_TO_SP -> {}
            case PULL_PROC_STATUS_FROM_SP -> {}

            case TRANSFER_SP_TO_X -> {}
            case TRANSFER_X_TO_SP -> {}

            default -> throw new UnsupportedOperationException("Unsupported instruction: " + instruction.name());
        }
        // execute the handler
        // write back to mem / reg
        // fetch the next instruction
    }

    private void readModifyWrite(ByteUnaryOperator operator) {
        @Unsigned byte data = registers.dor().getData(); // read
        registers.dor().setData(data); // write unmodified
        @Unsigned byte newData = operator.apply(data); // modify
        registers.dor().setData(newData); // write
    }

    private void branchIf(boolean condition) {
        @Unsigned short jumpAddress = registers.dor().getAddress();
        @Unsigned short currentPc = registers.pc().get();

        int jumpAddressHi = uint(jumpAddress) >> 8;
        int pcHi = registers.pc().highAsInt();
        boolean pageChange = jumpAddressHi != pcHi;

        cycleCounter.maybeIncrement(condition);
        cycleCounter.maybeIncrement(condition && pageChange);
        registers.pc().set(condition ? jumpAddress : currentPc); // hopefully cmov
    }
}
