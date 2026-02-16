package net.novaware.nes.core.cpu;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.memory.MemoryMap;
import net.novaware.nes.core.cpu.unit.AddressGen;
import net.novaware.nes.core.cpu.unit.ArithmeticLogic;
import net.novaware.nes.core.cpu.unit.ControlUnit;
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

/**
 * https://web.archive.org/web/20221112231348if_/http://archive.6502.org/datasheets/rockwell_r650x_r651x.pdf
 * TODO: test cpu https://github.com/SingleStepTests/65x02
 * TODO: https://github.com/christopherpow/nes-test-roms
 */
@BoardScope
@SuppressWarnings("unused") // @Owned unit fields are annotated only
public class Cpu implements Interruptible, Synchronizable, Overflowable {

    private final List<SyncListener> syncListeners = new CopyOnWriteArrayList<>();

    @Owned private final CpuRegisters registers;
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

    private final List<Unit> units;

    @Inject
    public Cpu(
        CpuRegisters registers,

        ControlUnit controlUnit,
        AddressGen addressGen,
        ArithmeticLogic alu,
        InstructionDecoder decoder,
        InterruptLogic interrupts,
        LoadStore loadStore,
        MemoryMgmt mmu,
        PowerMgmt powerMgmt,
        PrefetchUnit prefetch,
        StackEngine stackEngine
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
            this.stackEngine = stackEngine
        );
    }

    /* package */ void initialize() {
        registers.getStackSegment().set(MemoryMap.STACK_SEGMENT_START);

        units.forEach(Unit::initialize);
    }

    /**
     * Perform the reset of the CPU
     */
    /* package */ void reset() {
        units.forEach(Unit::reset);
    }

    public void ready() {
        // NOTE: input signal that allows to halt or single cycle the processor
    }

    /**
     * Advance single instruction
     */
    // TODO: use one lone coder test assembly to verify ticks
    public void advance() {
        controlUnit.fetchOperand();
        // low byte
        // high byte*

        controlUnit.decode();
        // low byte*
        // high byte*

        controlUnit.execute();
        // read* (and optionally write original back)
        // modify
        // write

        // controlUnit.sampleInterrupts();

        controlUnit.fetchOpcode();
    }

    /**
     * Trigger IRQ of CPU on low
     */
    @Override
    public void interruptRequest(boolean high) {

    }

    /**
     * Trigger NMI of CPU on low edge
     */
    @Override
    public void nonMaskableInterrupt(boolean high) {

    }

    /**
     * Trigger RST of the CPU on low
     */
    @Override
    public void reset(boolean high) {

    }

    /**
     * Trigger RDY halt/step the CPU on low edge
     */
    @Override
    public void ready(boolean high) {

    }

    @Override
    public void addSyncListener(SyncListener listener) {
        syncListeners.add(listener);
    }

    @Override
    public void removeSyncListener(SyncListener listener) {
        syncListeners.remove(listener);
    }

    protected void fireSyncChange(boolean high) {
        for (SyncListener listener : syncListeners) {
            listener.onSyncChange(high);
        }
    }

    @Override
    public void setOverflow(boolean high) {

    }
}
