package net.novaware.nes.core.memory

import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.*

class PagedMemorySpec extends Specification {

    def "should construct with fallback only"() {
        given:
        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", fallback)
        def bus = new TestBus(paged)

        when:
        def read = bus.access(USHORT_0).read().data()
        bus.access(USHORT_MAX_VALUE).write().data(ubyte(0x12))

        then:
        paged.getName() == "TEST"
        paged.getStartAddress() == USHORT_0
        paged.getEndAddress() == USHORT_MAX_VALUE

        read == ubyte(0xFF) // open bus, initial data line value

        1 * fallback.onAccess(ushort(0x0000))
        1 * fallback.onRead()

        1 * fallback.onAccess(ushort(0xFFFF))
        1 * fallback.onWrite()
    }

    def "should allow attaching devices"() {
        given:
        def ppuRegs = new PhysicalMemory("PPU", PPU_REGISTERS_START, PPU_REGISTERS_MIRROR_END,
        PPU_REGISTERS_MIRROR_SIZE)

        def ppuBus = new TestBus(ppuRegs)
        ppuBus.access(PPU_REGISTERS_START).write().data(ubyte(0x12))
        ppuBus.access(PPU_REGISTERS_MIRROR_END).write().data(ubyte(0x34))
        ppuRegs.onDetach()

        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", fallback)
        paged.attach(ppuRegs)

        def bus = new TestBus(paged)

        expect:
        bus.access(PPU_REGISTERS_START).read().data() == ubyte(0x12)
        bus.access(PPU_REGISTERS_MIRROR_END).read().data() == ubyte(0x34)
    }

    def "should disallow replacing devices"() {
        given:
        def ppuRegs1 = new PhysicalMemory("PPU1", PPU_REGISTERS_START, PPU_REGISTERS_MIRROR_END,
                PPU_REGISTERS_MIRROR_SIZE)

        def ppuRegs2 = new PhysicalMemory("PPU2", PPU_REGISTERS_START, PPU_REGISTERS_MIRROR_END,
                PPU_REGISTERS_MIRROR_SIZE)

        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", fallback)
        paged.attach(ppuRegs1)

        when:
        paged.attach(ppuRegs2)

        then:
        def thrown = thrown(IllegalArgumentException)
        thrown.message == "Attempting to replace R PPU1 (2000:3FFF) with PPU2 (2000:3FFF)"
    }
}
