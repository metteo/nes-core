package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BRK;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.signal.Signal.HIGH;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class InterruptLogic implements Unit {

    @Used
    private final CpuRegFile registers;

    @Used
    private final AddressGen agu;

    @Used
    private final MemoryMgmt mmu;

    @Used
    private final StackEngine stackEngine;

    @Used
    private final LevelDetector brkDetector;

    @Used
    private final LevelDetector irqDetector;

    @Used
    private final EdgeDetector  nmiDetector;

    @Used
    private final LevelDetector resDetector;

    @Used
    private final ShortRegister nmiVector;

    @Used
    private final ShortRegister irqVector;

    @Used
    private final ShortRegister resVector;

    @Inject
    public InterruptLogic(
            CpuRegFile registers,
            StackEngine stackEngine,
            AddressGen agu,
            MemoryMgmt mmu,
            @CpuVar(BRK) LevelDetector brkDetector,
            @CpuVar(IRQ) LevelDetector irqDetector,
            @CpuVar(NMI) EdgeDetector  nmiDetector,
            @CpuVar(RES) LevelDetector resDetector,
            @CpuVar(NMI) ShortRegister nmiVector,
            @CpuVar(IRQ) ShortRegister irqVector,
            @CpuVar(RES) ShortRegister resVector
    ) {
        this.registers = registers;
        this.stackEngine = stackEngine;
        this.agu = agu;
        this.mmu = mmu;
        this.brkDetector = brkDetector;
        this.irqDetector = irqDetector;
        this.nmiDetector = nmiDetector;
        this.resDetector = resDetector;
        this.nmiVector = nmiVector;
        this.irqVector = irqVector;
        this.resVector = resVector;
    }

    private @Unsigned short fetchVector() {
        @Unsigned short vector = USHORT_0;

        if (brkDetector.isActive()) { vector = irqVector.get(); }
        if (irqDetector.isActive()) { vector = irqVector.get(); }
        if (nmiDetector.isActive()) { vector = nmiVector.get(); }
        if (resDetector.isActive()) { vector = resVector.get(); }

        if (vector == USHORT_0) {
            throw new IllegalStateException("Correct vector should be selected at this point!");
        }

        return agu.fetchAddress(vector);
    }

    /**
     * Example 9.1: Illustration of Start Cycle
     * https://web.archive.org/web/20200129081101/http://users.telenet.be:80/kim1-6502/6502/proman.html#126
     */
    public void hardwareReset() {
        int randomAddress = 0x0000;                        // Cycles
        mmu.specifyAnd(ushort(randomAddress)).readByte();  // 1
        mmu.specifyAnd(ushort(randomAddress + 1)).readByte();// 2

        int sp = registers.sp().getAsInt();

        registers.sp().setAsByte(sp);
        mmu.specifyAnd(ushort(sp)).readByte();             // 3

        sp -= 1;
        registers.sp().setAsByte(sp);
        mmu.specifyAnd(ushort(sp)).readByte();             // 4

        sp -= 2;
        registers.sp().setAsByte(sp);
        mmu.specifyAnd(ushort(sp)).readByte();             // 5

        registers.pc().set(fetchVector());                 // 6, 7
    }

    public void forceBreak() {
        brkDetector.set(LOW);
    }

    /**
     * @see ControlUnit#fetchOpcode()
     * @see ControlUnit#fetchOperand()
     */
    private void softwareInterrupt() {                     // Cycles
        /* ControlUnit#fetchOpcode() */                    // 1
        /* ControlUnit#fetchOperand() */                   // 2

        performInterrupt();                                // 3 - 7
    }

    private void hardwareInterrupt() {
        int pcVal = registers.pc().getAsInt();             // Cycles
        mmu.specifyAnd(ushort(pcVal)).readByte();          // 1
        mmu.specifyAnd(ushort(pcVal + 1)).readByte();      // 2

        performInterrupt();                                // 3 - 7
    }

    private void performInterrupt() {
        stackEngine.push(registers.pc().get());            // 3, 4
        stackEngine.pushStatus(brkDetector.isActive());    // 5

        registers.status().setIrqDisabled(true);

        registers.pc().set(fetchVector());                 // 6, 7
    }

    public void returnFromInterrupt() {
        stackEngine.pullStatus();
        stackEngine.pull(registers.pc());
    }

    public void sample() {
        if (nmiDetector.isActive()) {
            hardwareInterrupt();
            // TODO: consider pulling the signal high, to prevent nmi loop
            return;
        }

        if (irqDetector.isActive() && !registers.status().isIrqDisabled()) {
            hardwareInterrupt();
            return;
        }

        if (brkDetector.isActive()) {
            softwareInterrupt();
            brkDetector.set(HIGH);
        }
    }
}
