package net.novaware.nes.core.cpu.memory

import net.novaware.nes.core.util.RegsAndRamBaseSpec

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class CpuBusCT extends RegsAndRamBaseSpec {

    def "should work"() {
        when:
        bus.access(ushort(address)).write().data(ubyte(data))

        then:
        bus.access(ushort(address)).read().data() == ubyte(data)

        where:
        address | data
        0x0123  | 0x45
    }
}
