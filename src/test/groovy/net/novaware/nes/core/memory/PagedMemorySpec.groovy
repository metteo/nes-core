package net.novaware.nes.core.memory

import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PagedMemorySpec extends Specification {

    def "should construct with open bus only"() {
        given:
        MemoryDevice.ReadWrite openBus = Mock()
        def paged = new PagedMemory(openBus)

        when:
        def read = paged.access(ushort(0x0000)).read().data()
        paged.access(ushort(0xFFFF)).write().data(ubyte(0x12))

        then:
        read == ubyte(0x34)

        1 * openBus.onAccess(ushort(0x0000))
        1 * openBus.onRead() >> ubyte(0x34) // legitimate read
        1 * openBus.onWrite(ubyte(0x34))    // remember last data bus value

        1 * openBus.onAccess(ushort(0xFFFF))
        1 * openBus.onWrite(ubyte(0x12)) // legitimate write
        1 * openBus.onWrite(ubyte(0x12)) // remember last data bus value
    }

    def "should allow attaching devices"() {
        given:
        MemoryDevice.ReadWrite openBus = Mock()
        def paged = new PagedMemory(openBus)

        def rom = new PhysicalMemory("CART", CARTRIDGE_START, CARTRIDGE_END, CARTRIDGE_SIZE)
        rom.specifyThen(ushort(CARTRIDGE_START)).writeByte(ubyte(0x12))
        rom.specifyThen(ushort(CARTRIDGE_END)).writeByte(ubyte(0x34))

        def replaced = paged.attach(rom)

        when:
        def cartStart = paged.access(ushort(CARTRIDGE_START)).read().data()
        def cartEnd = paged.access(ushort(CARTRIDGE_END)).read().data()

        then:
        replaced.isEmpty()

        cartStart == ubyte(0x12)
        cartEnd == ubyte(0x34)
    }
}
