package net.novaware.nes.core.memory

import net.novaware.nes.core.register.CycleCounter
import spock.lang.Specification

import static net.novaware.nes.core.util.UnsignedTypes.ubyte
import static net.novaware.nes.core.util.UnsignedTypes.uint
import static net.novaware.nes.core.util.UnsignedTypes.ushort

class SystemBusSpec extends Specification {

    def cycleCounter = new CycleCounter("CPUCC")

    def "should read and write to ram"() {
        given:
        SystemBus bus = new SystemBus(cycleCounter)
        def address = ushort(0x0002)
        def data = ubyte(0x3)

        when:
        bus.specifyAnd(address).writeByte(data)
        def read = bus.readByte()

        then:
        uint(read) == uint(data)

        and:
        bus.specifyAnd(ushort(0x4)).writeByte(ubyte(0x5))

        then:
        bus.specify(address)
        uint(bus.readByte()) == uint(data)

        // TODO: verify mirroring works
    }

    def "should read and write to ppu register"() {
        given:
        SystemBus bus = new SystemBus(cycleCounter)
        def address = ushort(0x2004)
        def data = ubyte(0xAA)

        when:
        bus.specifyAnd(address).writeByte(data)
        def read = bus.readByte()

        then:
        uint(read) == uint(data)
        bus.getPpuRegs(4).getAsInt() == uint(data)

        and:
        bus.specify(ushort(0x2004 + 0x8))
        def read2 = bus.readByte()

        then:
        uint(read2) == uint(data)
    }

    // TODO: write tests for apu and cartridge
}
