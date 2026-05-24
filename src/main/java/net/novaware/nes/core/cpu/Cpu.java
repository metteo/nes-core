package net.novaware.nes.core.cpu;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.signal.Interruptible;
import net.novaware.nes.core.cpu.signal.Overflowable;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.Synchronizable;
import net.novaware.nes.core.cpu.unit.AddressGen;
import net.novaware.nes.core.cpu.unit.ArithmeticLogic;
import net.novaware.nes.core.cpu.unit.ControlUnit;
import net.novaware.nes.core.cpu.unit.DiagnosticUnit;
import net.novaware.nes.core.cpu.unit.InstructionDecoder;
import net.novaware.nes.core.cpu.unit.InterruptLogic;
import net.novaware.nes.core.cpu.unit.LoadStore;
import net.novaware.nes.core.cpu.unit.MemoryMgmt;
import net.novaware.nes.core.cpu.unit.PowerMgmt;
import net.novaware.nes.core.cpu.unit.PrefetchUnit;
import net.novaware.nes.core.cpu.unit.StackEngine;
import net.novaware.nes.core.cpu.unit.Unit;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;

import java.util.List;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IRQ;
import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RDY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.inject.CpuVarName.S0H;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SOV;
import static net.novaware.nes.core.cpu.signal.Signal.HIGH;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;

/**
 * https://web.archive.org/web/20221112231348if_/http://archive.6502.org/datasheets/rockwell_r650x_r651x.pdf
 * TODO: test cpu https://github.com/SingleStepTests/65x02
 * TODO: https://github.com/christopherpow/nes-test-roms
 */
@BoardScope
@SuppressWarnings("unused") // @Owned unit fields are annotated only
public class Cpu implements Interruptible, Synchronizable, Overflowable, ClockReceiver { // TODO: These interfaces should be signal Senders / pins

    private SyncListener syncListener = signal -> {};

    @Owned private final CpuRegFile registers;
    @Owned private final ControlUnit controlUnit;

    @Owned private final AddressGen addressGen;
    @Owned private final ArithmeticLogic alu;
    @Owned private final InstructionDecoder decoder;
    @Owned private final InterruptLogic interrupts;
    @Owned private final LoadStore loadStore;
    @Owned private final MemoryMgmt mmu;
    @Owned private final PowerMgmt powerMgmt;
    @Owned private final PrefetchUnit prefetch;
    @Owned private final StackEngine stackEngine;
    @Owned private final DiagnosticUnit diagnostics;

    @Owned private final Pin irq;
    @Owned private final Pin nmi;
    @Owned private final Pin s0h;
    @Owned private final Pin so;

    @Owned private final Pin rdyPin;
    @Owned private final BooleanRegister rdyReg;

    @Owned private final Pin resPin;
    @Owned private final BooleanRegister resReg;

    @Used  private final List<Unit> units;

    @Owned private final IntegerCounter cycleCounter;
    @Owned private final IntegerCounter instructionCycle;

    @Inject
    public Cpu(
        CpuRegFile registers,

        ControlUnit controlUnit,
        AddressGen addressGen,
        ArithmeticLogic alu,
        InstructionDecoder decoder,
        InterruptLogic interrupts,
        LoadStore loadStore,
        MemoryMgmt mmu,
        PowerMgmt powerMgmt,
        PrefetchUnit prefetch,
        StackEngine stackEngine,
        DiagnosticUnit diagnostics,

        @CpuVar(CC)  IntegerCounter cycleCounter,
        @CpuVar(IC)  IntegerCounter instructionCycle,
        @CpuVar(IRQ) Pin irq,
        @CpuVar(NMI) Pin nmi,
        @CpuVar(S0H) Pin s0h,
        @CpuVar(SOV) Pin so,

        @CpuVar(RDY) Pin rdyPin,
        @CpuVar(RDY) BooleanRegister rdyReg,

        @CpuVar(RES) Pin resPin,
        @CpuVar(RES) BooleanRegister resReg
    ) {
        this.registers = registers;


        this.units = List.of(
            this.controlUnit = controlUnit,

            this.addressGen = addressGen,
            this.alu = alu,
            this.decoder = decoder,
            this.interrupts = interrupts,
            this.loadStore = loadStore,
            this.mmu = mmu,
            this.powerMgmt = powerMgmt,
            this.prefetch = prefetch,
            this.stackEngine = stackEngine,
            this.diagnostics = diagnostics
        );

        this.cycleCounter = cycleCounter;
        this.instructionCycle = instructionCycle;

        this.irq = irq;
        this.nmi = nmi;
        this.s0h = s0h;
        this.so = so;

        this.rdyPin = rdyPin;
        this.rdyReg = rdyReg;

        this.resPin = resPin;
        this.resReg = resReg;
    }

    public void initialize() {
        units.forEach(Unit::initialize);
    }

    /**
     * Perform the reset of the CPU
     */
    /* package */ void reset() {
        units.forEach(Unit::reset);
    }

    @Override
    public int cycle() {
        return advance();
    }

    /**
     * Advance single instruction
     *
     * @return number of used cycles
     */
    // TODO: use one lone coder test assembly to verify ticks
    public int advance() { // TODO: consider renaming to step()
        instructionCycle.reset();

        if (resReg.get()) {
            reset();
            return instructionCycle.getValue();
        }

        if (rdyReg.get()) {
            // TODO: repeat last bus.read operation which consumes 1 cycle!
            // Will it conflict with DMA?!
            cycleCounter.increment();
            instructionCycle.increment();
            return instructionCycle.getValue();
        }

        controlUnit.fetchOperand();
        // low byte
        // high byte*

        controlUnit.decode();
        // low byte*
        // high byte*
        // indirect fetch* // TODO: maybe it should be part of execute?

        diagnostics.run();

        controlUnit.execute();
        // read* (and optionally write original back)
        // modify
        // write

        // TODO: pending NMI latch set on falling edge of nmi (which is checked every cpu cycle)
        controlUnit.sampleInterrupts(); // TODO: verify that irqDisable delay is honored

        controlUnit.commitAll();

        syncListener.sync(HIGH);
        controlUnit.fetchOpcode();
        syncListener.sync(LOW);

        return instructionCycle.getValue();
    }

    /**
     * Trigger IRQ of CPU on low
     */
    @Override
    public void interruptRequest(Signal s) {
        irq.set(s);
    }

    /**
     * Trigger NMI of CPU on low edge
     */
    @Override
    public void nonMaskableInterrupt(Signal s) {
        nmi.set(s);
    }

    /**
     * Keep triggering RES of the CPU on low
     */
    @Override
    public void reset(Signal s) {
        resPin.set(s);
    }

    /**
     * Trigger S0H of cpu on low
     */
    @Override
    public void sprite0Hit(Signal s) {
        s0h.set(s);

        // TODO: decide if should wake up the cpu and jump to ppu status check?
    }

    /**
     * Trigger RDY halt/step the CPU on low edge
     */
    @Override
    public void ready(Signal s) {
        rdyPin.set(s);
    }

    @Override
    public void setSyncListener(SyncListener listener) {
        syncListener = listener;
    }

    @Override
    public void clearSyncListener() {
        syncListener = signal -> {};
    }

    /**
     * Set Overflow Processor Status flag on negative edge
     * @param s
     */
    @Override
    public void setOverflow(Signal s) {
        so.set(s);
    }
}
