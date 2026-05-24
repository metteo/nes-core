package net.novaware.nes.core.memory

import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.ProbeUtil.probeBus
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class MemoryPageSpec extends Specification {

    def "should construct with fallback only"() {
        given:
        MemoryDevice.ReadWrite fallback = Mock()
        def page = new MemoryPage(ubyte(0x40), fallback)
        def bus = new TestBus(page)

        when:
        def read = bus.access(ushort(0x4000)).read().data()
        bus.access(ushort(0x40FF)).write().data(ubyte(0x12))

        then:
        page.toString() == "PAGE 0x40 (4000:40FF)"
        page.getStartAddress() == ushort(0x4000)
        page.getEndAddress() == ushort(0x40FF)

        read == ubyte(0xFF) // open bus, initial data line value

        1 * fallback.onAccess(ushort(0x4000))
        1 * fallback.onRead()

        1 * fallback.onAccess(ushort(0x40FF))
        1 * fallback.onWrite()
    }

    def "should allow probing"() {
        given:
        MemoryDevice.ReadWrite fallback = Mock()
        def page = new MemoryPage(ubyte(0x40), fallback)
        def bus = new TestBus(page)

        when:
        def read = probeBus(bus, 0x40FF)

        then:
        read == 0x12

        1 * fallback.probe(ushort(0x40FF), _) >> { address, dataLine -> dataLine.data(ubyte(0x12)) }
    }

    def "should allow attaching devices"() {
        given:
        def apuRegs = new PhysicalMemory("APU", APU_REGISTERS_START, APU_REGISTERS_END,
                APU_REGISTERS_SIZE)

        def apuBus = new TestBus(apuRegs)
        apuBus.access(APU_REGISTERS_START).write().data(ubyte(0x12))
        apuBus.access(APU_REGISTERS_END).write().data(ubyte(0x34))
        apuRegs.onDetach()

        MemoryDevice.ReadWrite fallback = Mock()
        def page = new MemoryPage(ubyte(0x40), fallback)
        page.attach(apuRegs)

        def bus = new TestBus(page)

        expect:
        bus.access(APU_REGISTERS_START).read().data() == ubyte(0x12)
        bus.access(APU_REGISTERS_END).read().data() == ubyte(0x34)
    }

    def "should disallow replacing devices"() {
        given:
        def apuRegs1 = new PhysicalMemory("APU1", APU_REGISTERS_START, APU_REGISTERS_END,
                APU_REGISTERS_SIZE)

        def apuRegs2 = new PhysicalMemory("APU2", APU_REGISTERS_START, APU_REGISTERS_END,
                APU_REGISTERS_SIZE)

        MemoryDevice.ReadWrite fallback = Mock()
        def page = new MemoryPage(ubyte(0x40), fallback)
        page.attach(apuRegs1)

        when:
        page.attach(apuRegs2)

        then:
        def thrown = thrown(IllegalArgumentException)
        thrown.message == "Attempting to replace R APU1 (4000:4013) with APU2 (4000:4013)"
    }
}
