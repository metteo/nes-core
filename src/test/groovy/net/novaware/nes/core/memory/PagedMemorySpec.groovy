package net.novaware.nes.core.memory

import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.*

class PagedMemorySpec extends Specification {

    def "should construct with fallback only"() {
        given:
        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", size, fallback)
        def bus = new TestBus(paged)
        def lastAddr = ushort(size - 1)

        when:
        def read = bus.access(USHORT_0).read().data()
        bus.access(lastAddr).write().data(ubyte(0x12))

        then:
        paged.getName() == "TEST"
        paged.getStartAddress() == USHORT_0
        paged.getEndAddress() == lastAddr

        read == ubyte(0xFF) // open bus, initial data line value

        paged.allocatedPageCount == allocated

        1 * fallback.onAccess(ushort(0x0000))
        1 * fallback.onRead()

        1 * fallback.onAccess(lastAddr)
        1 * fallback.onWrite()

        where:
        size    | allocated | dev
        0x10000 | 0x100 * 2 | "cpu"
        0x4000  | 0x40 * 2  | "ppu"
    }

    def "should allow attaching cpu devices"() {
        given:
        def ppuRegs = new PhysicalMemory("PPU", PPU_REGISTERS_START, PPU_REGISTERS_MIRROR_END,
        PPU_REGISTERS_MIRROR_SIZE)

        def ppuBus = new TestBus(ppuRegs)
        ppuBus.access(PPU_REGISTERS_START).write().data(ubyte(0x12))
        ppuBus.access(PPU_REGISTERS_MIRROR_END).write().data(ubyte(0x34))
        ppuRegs.onDetach()

        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", 0x10000, fallback)
        paged.attach(ppuRegs)

        def bus = new TestBus(paged)

        expect:
        bus.access(PPU_REGISTERS_START).read().data() == ubyte(0x12)
        bus.access(PPU_REGISTERS_MIRROR_END).read().data() == ubyte(0x34)
    }

    def "should allow attaching ppu devices"() {
        given:
        def rom = new PhysicalMemory("CHR-ROM",
                PATTERN_TABLE_1_START,
                PATTERN_TABLE_2_END,
                PATTERN_TABLE_1_SIZE + PATTERN_TABLE_2_SIZE
        )

        def romBus = new TestBus(rom)
        romBus.access(PATTERN_TABLE_1_START).write().data(ubyte(0x12))
        romBus.access(PATTERN_TABLE_2_END).write().data(ubyte(0x34))
        rom.onDetach()

        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", 0x4000, fallback)
        paged.attach(rom)

        def bus = new TestBus(paged)

        expect:
        bus.access(PATTERN_TABLE_1_START).read().data() == ubyte(0x12)
        bus.access(PATTERN_TABLE_2_END).read().data() == ubyte(0x34)
    }

    def "should disallow replacing cpu devices"() {
        given:
        def ppuRegs1 = new PhysicalMemory("PPU1", PPU_REGISTERS_START, PPU_REGISTERS_MIRROR_END,
                PPU_REGISTERS_MIRROR_SIZE)

        def ppuRegs2 = new PhysicalMemory("PPU2", PPU_REGISTERS_START, PPU_REGISTERS_MIRROR_END,
                PPU_REGISTERS_MIRROR_SIZE)

        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", 0x10000, fallback)
        paged.attach(ppuRegs1)

        when:
        paged.attach(ppuRegs2)

        then:
        def thrown = thrown(IllegalArgumentException)
        thrown.message == "Attempting to replace R PPU1 (2000:3FFF) with PPU2 (2000:3FFF)"
    }

    Random random = new Random()

    def randomAddress() {
        ushort(random.nextInt(0xFFFF))
    }

    def "should allow detaching devices"() {
        given:
        def size = 0x10000
        def mem = new PhysicalMemory("MEM", USHORT_0, USHORT_MAX_VALUE, size)

        MemoryDevice.ReadWrite fallback = Mock()
        def paged = new PagedMemory("TEST", size, fallback)
        paged.attach(mem)
        paged.detach(mem)

        def bus = new TestBus(paged)

        def randomRead = randomAddress()
        def randomWrite = randomAddress()

        when:
        bus.access(randomRead).read().data()
        bus.access(randomWrite).write().data(ubyte(0x12))

        then:
        1 * fallback.onAccess(randomRead)
        1 * fallback.onRead()

        1 * fallback.onAccess(randomWrite)
        1 * fallback.onWrite()
    }
}
