package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.TestBoardFactory
import net.novaware.nes.core.memory.RecordingBus
import net.novaware.nes.core.util.RegsAndRamBaseSpec

class ControlUnitBaseSpec extends RegsAndRamBaseSpec<RecordingBus> {

    def factory = TestBoardFactory.newTestBoardFactory()
    def extRegisters = factory.newExtRegisters()

    def setup() {
        registers = factory.newCpuRegisters()
        bus = factory.newCpuBus() as RecordingBus
    }

    ControlUnit newControlUnit() {
        def cu = factory.newControlUnit()

        cu.initialize()
        cu
    }
}
