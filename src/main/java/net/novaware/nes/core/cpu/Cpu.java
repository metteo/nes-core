package net.novaware.nes.core.cpu;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.cpu.signal.Interruptible;
import net.novaware.nes.core.cpu.signal.Overflowable;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.Synchronizable;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
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
import net.novaware.nes.core.util.uml.Owned;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class Cpu implements Interruptible, Synchronizable, Overflowable {

    // TODO: No need for multiple listeners or thread safety, this is board only, and hidden behind a port otherwise
    private final List<SyncListener> syncListeners = new CopyOnWriteArrayList<>();

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

    @Owned private final LevelDetector irq;
    @Owned private final EdgeDetector nmi;
    @Owned private final LevelDetector s0h;
    @Owned private final LevelDetector res;
    @Owned private final LevelDetector rdy;
    @Owned private final EdgeDetector so;

    private final List<Unit> units;

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

        @CpuVar(IRQ) LevelDetector irq,
        @CpuVar(NMI) EdgeDetector nmi,
        @CpuVar(S0H) LevelDetector s0h,
        @CpuVar(RES) LevelDetector res,
        @CpuVar(RDY) LevelDetector rdy,
        @CpuVar(SOV) EdgeDetector so
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

        this.irq = irq;
        this.nmi = nmi;
        this.s0h = s0h;
        this.res = res;
        this.rdy = rdy;
        this.so = so;
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

    /**
     * Advance single instruction
     */
    // TODO: use one lone coder test assembly to verify ticks
    public void advance() { // TODO: consider renaming to step()
        if (rdy.isActive()) {
            // TODO: repeat last bus.read operation?
            return;
        }

        if (res.isActive()) {
            reset();
            return;
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

        controlUnit.sampleInterrupts(); // TODO: verify that irqDisable delay is honored

        controlUnit.commitAll();

        fireSyncChange(HIGH);
        controlUnit.fetchOpcode();
        fireSyncChange(LOW);

        // TODO: allow running for given cycle budget, efficiently stop before (or just after depending on strictness)
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
        res.set(s);
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
        rdy.set(s);
    }

    @Override
    public void addSyncListener(SyncListener listener) {
        syncListeners.add(listener);
    }

    @Override
    public void removeSyncListener(SyncListener listener) {
        syncListeners.remove(listener);
    }

    protected void fireSyncChange(Signal s) {
        if (syncListeners.isEmpty()) { return; }

        for (SyncListener listener : syncListeners) {
            listener.onSyncChange(s);
        }
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
