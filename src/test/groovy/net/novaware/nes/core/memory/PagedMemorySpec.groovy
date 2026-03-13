package net.novaware.nes.core.memory


import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PagedMemorySpec extends Specification {

    def "should construct with fallback only"() {
        given:
        MemoryDevice fallback = Mock()
        def paged = new PagedMemory(fallback)

        when:
        def start = paged.getStartAddress()
        def end = paged.getEndAddress()

        def read = paged.specifyThen(ushort(0x0000)).readByte()
        paged.specifyThen(ushort(0xFFFF)).writeByte(ubyte(0x12))

        then:
        start == ushort(0x0000)
        end == ushort(0xFFFF)
        read == ubyte(0x34)

        1 * fallback.getStartAddress() >> ushort(0x0000)
        1 * fallback.getEndAddress() >> ushort(0xFFFF)

        1 * fallback.specify(ushort(0x0000))
        1 * fallback.readByte() >> ubyte(0x34)

        1 * fallback.specify(ushort(0xFFFF))
        1 * fallback.writeByte(ubyte(0x12))
    }

    def "should allow attaching devices"() {
        given:
        MemoryDevice fallback = Mock()
        def paged = new PagedMemory(fallback)

        def rom = new PhysicalMemory("CART", CARTRIDGE_START, CARTRIDGE_END, CARTRIDGE_SIZE)
        rom.specifyThen(ushort(CARTRIDGE_START)).writeByte(ubyte(0x12))
        rom.specifyThen(ushort(CARTRIDGE_END)).writeByte(ubyte(0x34))

        def replaced = paged.attach(rom)

        when:
        def start = paged.getStartAddress()
        def end = paged.getEndAddress()

        def cartStart = paged.specifyThen(ushort(CARTRIDGE_START)).readByte()
        def cartEnd = paged.specifyThen(ushort(CARTRIDGE_END)).readByte()

        then:
        replaced.isEmpty()

        start == ushort(0x0000)
        end == ushort(0xFFFF)

        cartStart == ubyte(0x12)
        cartEnd == ubyte(0x34)

        1 * fallback.getStartAddress() >> ushort(0x0000)
    }
}
