package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.cpu.instruction.InstructionRegistry;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.register.InstructionRegister;
import net.novaware.nes.core.register.BooleanLatch;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.UByteUnaryOperator;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DO;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ID;

@BoardScope
public class ControlUnit implements Unit {

    @Owned private final ControlFlow flow;

    @Used private final CpuRegFile registers;
    @Used private final BooleanLatch interruptDisabled;

    @Used private final ByteRegister currentInstruction;
    @Used private final ShortRegister currentOperand;
    @Used private final InstructionRegister decodedInstruction;
    @Used private final DelegatingRegister decodedOperand;

    @Used private final IntegerCounter cycleCounter;
    @Used private final IntegerCounter instructionCycle;

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

        CpuRegFile registers,
        @CpuVar(ID) BooleanLatch interruptDisabled,
        @CpuVar(CI) ByteRegister currentInstruction,
        @CpuVar(CO) ShortRegister currentOperand,

        @CpuVar(DI) InstructionRegister decodedInstruction,
        @CpuVar(DO) DelegatingRegister decodedOperand,

        @CpuVar(CC) IntegerCounter cycleCounter,
        @CpuVar(IC) IntegerCounter instructionCycle,

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
        this.interruptDisabled = interruptDisabled;
        this.currentInstruction = currentInstruction;
        this.currentOperand = currentOperand;
        this.decodedInstruction = decodedInstruction;
        this.decodedOperand = decodedOperand;

        this.cycleCounter = cycleCounter;
        this.instructionCycle = instructionCycle;

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

        registers.a().setAsByte(0);
        registers.x().setAsByte(0);
        registers.y().setAsByte(0);
        registers.sp().setAsByte(0x00);
        registers.status().initialize();
    }

    @Override
    public void reset() {
        cycleCounter.reset();
        instructionCycle.reset();

        interruptDisabled.reset();
        registers.status().reset();

        flow.reset();

        interrupts.hardwareReset(); // 1 - 7
        prefetch.run();             // 8
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

        currentOperand.low(operandLo);
    }

    public void fetchOperandHi() {
        @Unsigned short operandHiAddress = addressGen.getPc();
        @Unsigned byte operandHi = mmu.specifyAnd(operandHiAddress).readByte();

        currentOperand.high(operandHi);
    }

    public void fetchOperand() {
        int size = InstructionRegistry.fromOpcode(currentInstruction.get()).size(); // TODO: consider dedicated instruction size index

        switch (size) {
            case 1 -> {
                mmu.specifyAnd(registers.pc().get()).readByte(); // no pc increment, data ignored
                currentOperand.lowAsByte(0x00);
                currentOperand.highAsByte(0x00);
            }
            case 2 -> {
                fetchOperandLo();
                currentOperand.highAsByte(0x00);
            }
            case 3 -> {
                fetchOperandLo();
                fetchOperandHi();
            }
            default -> throw new IllegalArgumentException("Unsupported instruction size:" + size);
        }
    }

    public void decode() {
        decoder.decode();
    }

    // TODO: test that correct units are called
    public void execute() {
        InstructionGroup instruction = decodedInstruction.getGroup();

        switch (instruction) { // TODO: make these 0 argument methods
            case ADD_WITH_CARRY       -> alu.addWithCarry(decodedOperand.getData());
            case SUBTRACT_WITH_BORROW -> alu.subtractWithBorrow(decodedOperand.getData());

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

            case COMPARE_A_WITH_MEMORY -> alu.compareA(decodedOperand.getData());
            case COMPARE_X_WITH_MEMORY -> alu.compareX(decodedOperand.getData());
            case COMPARE_Y_WITH_MEMORY -> alu.compareY(decodedOperand.getData());

            case JUMP_TO_LOCATION -> flow.jumpTo();

            case JUMP_TO_SUBROUTINE -> flow.call();
            case RETURN_FROM_SUBROUTINE -> flow.returnFromCall();

            case SET_CARRY -> registers.status().setCarry(true);
            case CLR_CARRY -> registers.status().setCarry(false);

            // TODO: http://www.6502.org/tutorials/decimal_mode.html
            case SET_DECIMAL -> registers.status().setDecimal(true);
            case CLR_DECIMAL -> registers.status().setDecimal(false);

            case SET_INTERRUPT_DISABLE -> interruptDisabled.delayedSet(true);
            case CLR_INTERRUPT_DISABLE -> interruptDisabled.delayedSet(false);

            case CLR_OVERFLOW -> registers.status().setOverflow(false);

            case FORCE_BREAK -> interrupts.forceBreak(); // TODO: what about unused operand that is skipped on return?
            case RETURN_FROM_INTERRUPT -> interrupts.returnFromInterrupt();

            case BITWISE_AND -> alu.bitwiseAnd(decodedOperand.getData());
            case BITWISE_OR -> alu.bitwiseOr();
            case BITWISE_XOR -> alu.bitwiseXor(decodedOperand.getData());
            case BIT_TEST -> alu.bitTest(decodedOperand.getData());

            case LOAD_A_WITH_MEMORY -> loadStore.load(registers.a());
            case STORE_A_IN_MEMORY -> loadStore.store(registers.a());

            case LOAD_X_WITH_MEMORY -> loadStore.load(registers.x());
            case STORE_X_IN_MEMORY -> loadStore.store(registers.x());

            case LOAD_Y_WITH_MEMORY -> loadStore.load(registers.y());
            case STORE_Y_IN_MEMORY -> loadStore.store(registers.y());

            case NO_OPERATION -> {}

            case TRANSFER_A_TO_X -> alu.transfer(registers.a(), registers.x());
            case TRANSFER_X_TO_A -> alu.transfer(registers.x(), registers.a());

            case TRANSFER_A_TO_Y -> alu.transfer(registers.a(), registers.y());
            case TRANSFER_Y_TO_A -> alu.transfer(registers.y(), registers.a());

            case SHIFT_LEFT  -> readModifyWrite(alu::arithmeticShiftLeft);
            case SHIFT_RIGHT -> readModifyWrite(alu::logicalShiftRight);

            case ROTATE_LEFT  -> readModifyWrite(alu::rotateLeft);
            case ROTATE_RIGHT -> readModifyWrite(alu::rotateRight);

            case PUSH_A_TO_SP   -> stackEngine.push(registers.a());
            case PULL_A_FROM_SP -> stackEngine.pull(registers.a());

            case PUSH_STATUS_TO_SP   -> stackEngine.pushStatus(true);
            case PULL_STATUS_FROM_SP -> stackEngine.pullStatusWithDelayedInterruptDisable();

            case TRANSFER_SP_TO_X -> alu.transfer(registers.sp(), registers.x());
            case TRANSFER_X_TO_SP -> registers.sp().set(registers.x().get()); // no flag updates

            case DEC_MEM_CMP_A -> { readModifyWrite(alu::decrementMemory); alu.compareA(decodedOperand.getData()); } // FIXME: test illegal

            default -> throw new UnsupportedOperationException("Unsupported instruction: " + instruction.name());
        }
    }

    public void commitAll() {
        interruptDisabled.commit();
    }

    /* package */ void readModifyWrite(UByteUnaryOperator operator) { // TODO: move to alu
        @Unsigned byte data = decodedOperand.getData(); // read
        decodedOperand.setData(data); // write unmodified

        @Unsigned byte newData = operator.applyAsUByte(data); // modify
        decodedOperand.setData(newData); // write
    }

    public void sampleInterrupts() {
        interrupts.sample();
    }


}
