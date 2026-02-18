package net.novaware.nes.core.cpu.unit


import net.novaware.nes.core.util.RegsAndRamBaseSpec

class ControlUnitBaseSpec extends RegsAndRamBaseSpec {

    ArithmeticLogic alu = new ArithmeticLogic(registers)
    MemoryMgmt mmu = new MemoryMgmt(registers, bus)
    AddressGen agu = new AddressGen(registers, mmu)
    InstructionDecoder decoder = new InstructionDecoder(registers, bus.cycleCounter(), bus, agu)
    LoadStore loadStore = new LoadStore(registers)
    StackEngine stackEngine = new StackEngine(registers, mmu)
    InterruptLogic interrupts = new InterruptLogic(registers, stackEngine)
    ControlFlow flow = new ControlFlow(registers, bus.cycleCounter(), stackEngine)
    PrefetchUnit prefetch = new PrefetchUnit(registers, agu, mmu)


    ControlUnit newControlUnit() {
        def cu = new ControlUnit(
            flow,
            registers,
            bus.cycleCounter(),
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
