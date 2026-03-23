package net.novaware.nes.core.memory


import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PagedMemorySpec extends Specification {

    def "should construct with open bus only"() {
        given:
        MemoryDevice.ReadWrite openBus = Mock()
        def paged = new PagedMemory(openBus)

        def tempLine = new DataLine()
        tempLine.data(ubyte(0x34)) // previous value for open bus case
        tempLine.cycle()

        when:
        paged.onAccess(ushort(0x0000))
        paged.onRead()
        def read = tempLine.cycle()
        paged.onAccess(ushort(0xFFFF))
        tempLine.data(ubyte(0x12))
        paged.onWrite()

        then:
        // TODO: start and end address too.
        read == ubyte(0x34)

        1 * openBus.onAccess(ushort(0x0000))
        1 * openBus.onRead()

        1 * openBus.onAccess(ushort(0xFFFF))
        1 * openBus.onWrite()
    }

    def "should allow attaching devices"() {
        given:
        MemoryDevice.ReadWrite openBus = Mock()
        def paged = new PagedMemory(openBus)

        def tempLine = new DataLine()

        def start = ushort(0x4100) // TODO: switch to 4020 when page 40 is implemented properly
        def size = sint(CARTRIDGE_END) - sint(start) + 1

        def rom = new PhysicalMemory("CART", start, CARTRIDGE_END, size)
        def romBus = new TestBus(rom)
        romBus.access(start).write().data(ubyte(0x12))
        romBus.access(CARTRIDGE_END).write().data(ubyte(0x34))

        def replaced = paged.attach(rom)

        paged.onAttach(tempLine)
        rom.onAttach(tempLine) // FIXME: should be handled during pagememory attachment

        when:
        paged.onAccess(start)
        paged.onRead()
        def cartStart = tempLine.cycle()

        paged.onAccess(CARTRIDGE_END)
        paged.onRead()
        def cartEnd = tempLine.cycle()

        then:
        replaced.isEmpty()

        cartStart == ubyte(0x12)
        cartEnd == ubyte(0x34)
    }
}
