package net.novaware.nes.core.cpu.memory

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.util.RegsAndRamBaseSpec

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class CpuBusCT extends RegsAndRamBaseSpec<CpuBus> {

    @Override
    MemoryBus.Type getCpuBusType() {
        return MemoryBus.Type.STANDARD
    }

    def "should work"() {
        when:
        bus.specifyThen(ushort(address)).writeByte(ubyte(data))

        then:
        bus.specifyThen(ushort(address)).readByte() == ubyte(data)

        where:
        address | data
        0x0123  | 0x45
    }
}
