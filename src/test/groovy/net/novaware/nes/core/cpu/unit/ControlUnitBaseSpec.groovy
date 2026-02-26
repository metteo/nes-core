package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.memory.RecordingBus
import net.novaware.nes.core.util.RegsAndRamBaseSpec

class ControlUnitBaseSpec extends RegsAndRamBaseSpec {

    RecordingBus recBus
    ArithmeticLogic alu
    MemoryMgmt mmu
    AddressGen agu
    InstructionDecoder decoder
    LoadStore loadStore
    StackEngine stackEngine
    InterruptLogic interrupts
    ControlFlow flow
    PrefetchUnit prefetch

    def setup() {
        registers = new CpuRegisters()
        recBus = new RecordingBus()
        bus = recBus

        alu = new ArithmeticLogic(registers)
        mmu = new MemoryMgmt(registers, bus)
        agu = new AddressGen(registers, mmu)
        decoder = new InstructionDecoder(registers, bus.cycleCounter(), bus, agu)
        loadStore = new LoadStore(registers)
        stackEngine = new StackEngine(registers.stackSegment, registers, mmu)
        interrupts = new InterruptLogic(registers, stackEngine)
        flow = new ControlFlow(registers, bus.cycleCounter(), stackEngine)
        prefetch = new PrefetchUnit(registers, agu, mmu)
    }

    ControlUnit newControlUnit() {
        def cu = new ControlUnit(
            flow,
            registers,
            recBus.cycleCounter(),
            agu,
            alu,
            decoder,
            interrupts,
            loadStore,
            mmu,
            prefetch,
            stackEngine
        )

        cu.initialize()
        cu
    }
}
