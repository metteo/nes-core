package net.novaware.nes.core.memory

import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.memory.IndexedMemory.AddressPart.OFFSET
import static net.novaware.nes.core.memory.IndexedMemory.AddressPart.PAGE
import static net.novaware.nes.core.util.UTypes.sint
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class IndexedMemorySpec extends Specification {

    def "should derive start and end from attached devices"() {
        given:
        def ram = new PhysicalMemory(0x2000, 0x0000)
        def sram = new PhysicalMemory(0x6000, 0x2000)
        def rom = new PhysicalMemory(0x8000, 0x8000)

        when:
        def index = new IndexedMemory(PAGE, ram, sram, rom)

        then:
        index.getStartAddress() == ushort(0x0000)
        index.getEndAddress() == ushort(0xFFFF)
    }

    def "should detect overlapping memory pages"() {
        given:
        def ram = new PhysicalMemory(0x2100, 0x0000)
        def sram = new PhysicalMemory(0x6000, 0x2000)
        def rom = new PhysicalMemory(0x8000, 0x8000)

        when:
        new IndexedMemory(PAGE, ram, sram, rom)

        then:
        def t = thrown(IllegalStateException)
        t.getMessage() == "Overlapping memory PAGEs!"
    }

    def "should detect undefined memory pages"() {
        given:
        def ram = new PhysicalMemory(0x1900, 0x0000)
        def sram = new PhysicalMemory(0x6000, 0x2000)
        def rom = new PhysicalMemory(0x8000, 0x8000)

        when:
        new IndexedMemory(PAGE, ram, sram, rom)

        then:
        def t = thrown(IllegalStateException)
        t.getMessage() == "Undefined memory PAGE!"
    }

    def "should direct to correct memory device (by page)"() {
        def ram = new PhysicalMemory(0x2000, 0x0000)
        ram.specifyThen(ushort(0x0000)).writeByte(ubyte(0x12))
        ram.specifyThen(ushort(0x1FFF)).writeByte(ubyte(0x34))

        def sram = new PhysicalMemory(0x6000, 0x2000)
        sram.specifyThen(ushort(0x2000)).writeByte(ubyte(0x56))
        sram.specifyThen(ushort(0x7FFF)).writeByte(ubyte(0x78))

        def rom = new PhysicalMemory(0x8000, 0x8000)
        rom.specifyThen(ushort(0x8000)).writeByte(ubyte(0x9A))
        rom.specifyThen(ushort(0xFFFF)).writeByte(ubyte(0xBC))

        when:
        def index = new IndexedMemory(PAGE, ram, sram, rom)

        then:
        index.specifyThen(ushort(0x0000)).readByte() == ubyte(0x12)
        index.specifyThen(ushort(0x1FFF)).readByte() == ubyte(0x34)

        index.specifyThen(ushort(0x2000)).readByte() == ubyte(0x56)
        index.specifyThen(ushort(0x7FFF)).readByte() == ubyte(0x78)

        index.specifyThen(ushort(0x8000)).readByte() == ubyte(0x9A)
        index.specifyThen(ushort(0xFFFF)).readByte() == ubyte(0xBC)
    }

    def "should direct to correct memory device (by offset)"() {
        def apu = new PhysicalMemory(APU_REGISTERS_SIZE, sint(APU_REGISTERS_START))
        apu.specifyThen(ushort(0x4000)).writeByte(ubyte(0x12))
        apu.specifyThen(ushort(0x4013)).writeByte(ubyte(0x34))

        def io = new PhysicalMemory(IO_REGISTERS_SIZE, sint(IO_REGISTERS_START))
        io.specifyThen(ushort(0x4014)).writeByte(ubyte(0x56))
        io.specifyThen(ushort(0x4017)).writeByte(ubyte(0x78))

        def apuTest = new PhysicalMemory(APU_TEST_REGISTERS_SIZE, sint(APU_TEST_REGISTERS_START))
        apuTest.specifyThen(ushort(0x4018)).writeByte(ubyte(0x9A))
        apuTest.specifyThen(ushort(0x401B)).writeByte(ubyte(0xBC))

        def timer = new PhysicalMemory(TIMER_REGISTERS_SIZE, sint(TIMER_REGISTERS_START))
        timer.specifyThen(ushort(0x401C)).writeByte(ubyte(0xDE))
        timer.specifyThen(ushort(0x401F)).writeByte(ubyte(0xF0))

        def fds = new PhysicalMemory(CARTRIDGE_FDS_SIZE, sint(CARTRIDGE_FDS_START))
        fds.specifyThen(ushort(0x4020)).writeByte(ubyte(0x0F))
        fds.specifyThen(ushort(0x40FF)).writeByte(ubyte(0xED))

        when:
        def index = new IndexedMemory(OFFSET, apu, io, apuTest, timer, fds)

        then:
        index.specifyThen(ushort(0x4000)).readByte() == ubyte(0x12)
        index.specifyThen(ushort(0x4013)).readByte() == ubyte(0x34)

        index.specifyThen(ushort(0x4014)).readByte() == ubyte(0x56)
        index.specifyThen(ushort(0x4017)).readByte() == ubyte(0x78)

        index.specifyThen(ushort(0x4018)).readByte() == ubyte(0x9A)
        index.specifyThen(ushort(0x401B)).readByte() == ubyte(0xBC)

        index.specifyThen(ushort(0x401C)).readByte() == ubyte(0xDE)
        index.specifyThen(ushort(0x401F)).readByte() == ubyte(0xF0)

        index.specifyThen(ushort(0x4020)).readByte() == ubyte(0x0F)
        index.specifyThen(ushort(0x40FF)).readByte() == ubyte(0xED)
    }
}
