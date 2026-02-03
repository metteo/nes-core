package net.novaware.nes.core.cpu;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.unit.AddressGen;
import net.novaware.nes.core.cpu.unit.ArithmeticLogic;
import net.novaware.nes.core.cpu.unit.ControlUnit;
import net.novaware.nes.core.cpu.unit.InstructionDecoder;
import net.novaware.nes.core.cpu.unit.InterruptLogic;
import net.novaware.nes.core.cpu.unit.LoadStore;
import net.novaware.nes.core.cpu.unit.PowerMgmt;
import net.novaware.nes.core.cpu.unit.StackEngine;
import net.novaware.nes.core.cpu.unit.Unit;
import net.novaware.nes.core.util.uml.Owned;

import java.util.List;

/**
 * https://web.archive.org/web/20221112231348if_/http://archive.6502.org/datasheets/rockwell_r650x_r651x.pdf
 * TODO: test cpu https://github.com/SingleStepTests/65x02
 * TODO: https://github.com/christopherpow/nes-test-roms
 */
@BoardScope
public class Cpu {

    @Owned private final CpuRegisters registers;

    @Owned private final ControlUnit controlUnit;
    @Owned private final AddressGen addressGen;
    @Owned private final ArithmeticLogic alu;
    @Owned private final InstructionDecoder decoder;
    @Owned private final InterruptLogic interrupts;
    @Owned private final LoadStore loadStore;
    @Owned private final PowerMgmt powerMgmt;
    @Owned private final StackEngine stackEngine;

    private List<Unit> units;

    @Inject
    public Cpu(
        CpuRegisters registers,

        ControlUnit controlUnit,
        AddressGen addressGen,
        ArithmeticLogic alu,
        InstructionDecoder decoder,
        InterruptLogic interrupts,
        LoadStore loadStore,
        PowerMgmt powerMgmt,
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
            this.powerMgmt = powerMgmt,
            this.stackEngine = stackEngine
        );
    }

    public void initialize() {
        units.forEach(Unit::initialize);
    }

    public void powerOn() {
        controlUnit.powerOn();
    }

    public void reset() {
        units.forEach(Unit::reset);
    }

    public void ready() {
        // NOTE: input signal that allows to halt or single cycle the processor
    }

    public void cycle() {
        controlUnit.execute();

        controlUnit.fetch();
        controlUnit.decode();
    }
}
