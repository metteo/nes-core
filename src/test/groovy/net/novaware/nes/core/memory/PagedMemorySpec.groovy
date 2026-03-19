package net.novaware.nes.core.memory


import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.memory.DataBus.*
import static net.novaware.nes.core.util.UTypes.*

class PagedMemorySpec extends Specification {

    def "should construct with open bus only"() {
        given:
        MemoryDevice.ReadWrite openBus = Mock()
        def paged = new PagedMemory(openBus)

        def tempLine = new TempLine()
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

        def tempLine = new TempLine()

        def rom = new PhysicalMemory("CART", CARTRIDGE_START, CARTRIDGE_END, CARTRIDGE_SIZE)
        rom.specifyThen(CARTRIDGE_START).writeByte(ubyte(0x12))
        rom.specifyThen(CARTRIDGE_END).writeByte(ubyte(0x34))

        def replaced = paged.attach(rom)

        paged.onAttach(tempLine)
        rom.onAttach(tempLine) // FIXME: should be handled during pagememory attachment

        when:
        paged.onAccess(CARTRIDGE_START)
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
