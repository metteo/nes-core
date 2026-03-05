package net.novaware.nes.core.cpu.unit


import net.novaware.nes.core.memory.RecordingBus
import net.novaware.nes.core.util.RegsAndRamBaseSpec

class ControlUnitBaseSpec extends RegsAndRamBaseSpec<RecordingBus> {

    ControlUnit newControlUnit() {
        def cu = factory.newControlUnit()

        cu.initialize()
        cu
    }
}
