package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.util.UByteUnaryOperator;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_CYCLE_COUNTER;
import static net.novaware.nes.core.util.UTypes.sint;

@BoardScope
public class ControlUnit implements Unit {

    // stabilizing after takes about n cycles, 6 cycles according to PDF
    private static final int INITIAL_CPU_CYCLE = 3;

    @Owned private final ControlFlow flow;

    @Used private final CpuRegisters registers;
    @Used private final CycleCounter cycleCounter;

    @Used private final AddressGen addressGen;
    @Used private final ArithmeticLogic alu;
    @Used private final InstructionDecoder decoder;
    @Used private final InterruptLogic interrupts;
    @Used private final LoadStore loadStore;
    @Used private final MemoryMgmt mmu;
    @Used private final PrefetchUnit prefetch;
    @Used private final StackEngine stackEngine;

    @Inject
    public ControlUnit(
            ControlFlow flow,

            CpuRegisters registers,
            @Named(CPU_CYCLE_COUNTER) CycleCounter cycleCounter,

            AddressGen addressGen,
            ArithmeticLogic alu,
            InstructionDecoder decoder,
            InterruptLogic interrupts,
            LoadStore loadStore,
            MemoryMgmt mmu,
            PrefetchUnit prefetch,
            StackEngine stackEngine

    ) {
        this.flow = flow;

        this.registers = registers;
        this.cycleCounter = cycleCounter;

        this.addressGen = addressGen;
        this.alu = alu;
        this.decoder = decoder;
        this.interrupts = interrupts;
        this.loadStore = loadStore;
        this.mmu = mmu;
        this.prefetch = prefetch;
        this.stackEngine = stackEngine;
    }

    @Override
    public void initialize() {
        flow.initialize();

        cycleCounter.setValue(INITIAL_CPU_CYCLE);

        registers.a().setAsByte(0);
        registers.x().setAsByte(0);
        registers.y().setAsByte(0);
        registers.sp().setAsByte(0xFD);
        registers.status().initialize();

        // TODO: move to InterruptLogic unit
        registers.pc().set(addressGen.fetchAddress(InterruptLogic.RES_VECTOR));
        prefetch.run();
    }

    @Override
    public void reset() {
        flow.reset();

        cycleCounter.setValue(INITIAL_CPU_CYCLE);

        registers.pc().set(addressGen.fetchAddress(InterruptLogic.RES_VECTOR));
        prefetch.run();

        registers.status().reset();

        // TODO: move to stack engine
        int sp = registers.sp().getAsInt();
        sp -= 3;
        registers.sp().setAsByte(sp);
    }

    /**
     * Last cycle of the instruction
     */
    public void fetchOpcode() {
        prefetch.run();
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

    // TODO: test that correct units are called
    public void execute() {
        int instrGroup = registers.dir().getAsInt();

        InstructionGroup instruction = InstructionGroup.valueOf(instrGroup);

        switch (instruction) {
            case ADD_WITH_CARRY       -> alu.addWithCarry(registers.dor().getData());
            case SUBTRACT_WITH_BORROW -> alu.subtractWithBorrow(registers.dor().getData());

            case INCREMENT_MEMORY -> readModifyWrite(alu::incrementMemory);
            case DECREMENT_MEMORY -> readModifyWrite(alu::decrementMemory);

            case INCREMENT_X -> alu.incrementX();
            case DECREMENT_X -> alu.decrementX();

            case INCREMENT_Y -> alu.incrementY();
            case DECREMENT_Y -> alu.decrementY();

            case BRANCH_IF_NEGATIVE_SET -> flow.branchIf(registers.status().isNegative());
            case BRANCH_IF_NEGATIVE_CLR -> flow.branchIf(!registers.status().isNegative());

            case BRANCH_IF_ZERO_SET     -> flow.branchIf(registers.status().isZero());
            case BRANCH_IF_ZERO_CLR     -> flow.branchIf(!registers.status().isZero());

            case BRANCH_IF_CARRY_SET    -> flow.branchIf(registers.status().getCarry());
            case BRANCH_IF_CARRY_CLR    -> flow.branchIf(!registers.status().getCarry());

            case BRANCH_IF_OVERFLOW_SET -> flow.branchIf(registers.status().isOverflow());
            case BRANCH_IF_OVERFLOW_CLR -> flow.branchIf(!registers.status().isOverflow());

            case COMPARE_A_WITH_MEMORY -> alu.compareA(registers.dor().getData());
            case COMPARE_X_WITH_MEMORY -> alu.compareX(registers.dor().getData());
            case COMPARE_Y_WITH_MEMORY -> alu.compareY(registers.dor().getData());

            case JUMP_TO_LOCATION -> flow.jumpTo();

            case JUMP_TO_SUBROUTINE -> flow.call();
            case RETURN_FROM_SUBROUTINE -> flow.returnFromCall();

            case SET_CARRY -> registers.status().setCarry(true);
            case CLR_CARRY -> registers.status().setCarry(false);

            // TODO: http://www.6502.org/tutorials/decimal_mode.html
            case SET_DECIMAL -> registers.status().setDecimal(true);
            case CLR_DECIMAL -> registers.status().setDecimal(false);

            case SET_INTERRUPT_DISABLE -> registers.status().setIrqDisabled(true);
            case CLR_INTERRUPT_DISABLE -> registers.status().setIrqDisabled(false);

            case CLR_OVERFLOW -> registers.status().setOverflow(false);

            case FORCE_BREAK -> interrupts.forceBreak(); // TODO: what about unused operand that is skipped on return?
            case RETURN_FROM_INTERRUPT -> interrupts.returnFromInterrupt();

            case BITWISE_AND -> alu.bitwiseAnd(registers.dor().getData());
            case BITWISE_OR -> alu.bitwiseOr(registers.dor().getData());
            case BITWISE_XOR -> alu.bitwiseXor(registers.dor().getData());
            case BIT_TEST -> alu.bitTest(registers.dor().getData());

            case LOAD_A_WITH_MEMORY -> loadStore.load(registers.a());
            case STORE_A_IN_MEMORY -> loadStore.store(registers.a());

            case LOAD_X_WITH_MEMORY -> loadStore.load(registers.x());
            case STORE_X_IN_MEMORY -> loadStore.store(registers.x());

            case LOAD_Y_WITH_MEMORY -> loadStore.load(registers.y());
            case STORE_Y_IN_MEMORY -> loadStore.store(registers.y());

            case NO_OPERATION -> {}

            case TRANSFER_A_TO_X -> this.transfer(registers.a(), registers.x());
            case TRANSFER_X_TO_A -> this.transfer(registers.x(), registers.a());

            case TRANSFER_A_TO_Y -> this.transfer(registers.a(), registers.y());
            case TRANSFER_Y_TO_A -> this.transfer(registers.y(), registers.a());

            case SHIFT_LEFT  -> readModifyWrite(alu::arithmeticShiftLeft);
            case SHIFT_RIGHT -> readModifyWrite(alu::logicalShiftRight);

            case ROTATE_LEFT  -> readModifyWrite(alu::rotateLeft);
            case ROTATE_RIGHT -> readModifyWrite(alu::rotateRight);

            case PUSH_A_TO_SP   -> stackEngine.push(registers.a());
            case PULL_A_FROM_SP -> stackEngine.pull(registers.a());

            case PUSH_STATUS_TO_SP   -> stackEngine.pushStatus();
            case PULL_STATUS_FROM_SP -> stackEngine.pullStatus();

            case TRANSFER_SP_TO_X -> this.transfer(registers.sp(), registers.x());
            case TRANSFER_X_TO_SP -> registers.sp().set(registers.x().get()); // no flag updates

            default -> throw new UnsupportedOperationException("Unsupported instruction: " + instruction.name());
        }
        // execute the handler
        // write back to mem / reg
        // fetch the next instruction
    }

    /* package */ void readModifyWrite(UByteUnaryOperator operator) {
        @Unsigned byte data = registers.dor().getData(); // read
        registers.dor().setData(data); // write unmodified

        @Unsigned byte newData = operator.applyAsUByte(data); // modify
        registers.dor().setData(newData); // write
    }

    /* package */ void transfer(DataRegister src, DataRegister dst) {
        @Unsigned byte data = src.get();

        dst.set(data);

        int dataVal = sint(data);

        registers.status()
                .setZero(dataVal == 0)
                .setNegative((dataVal & 0x80) != 0);
    }
}
