package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte

class NameTableSpec extends Specification {

    def segment = PpuMemModule.provideNameTable0Segment()

    def "should construct an instance"() {
        given:
        MemoryBus bus = Mock()

        when:
        def instance = new NameTable("test", segment, bus)

        then:
        instance.getName() == "test"
        instance.toString() == "test (2000:23BF)"
    }

    def "should properly read name table corners"() {
        given:
        def nameTable0 = new PhysicalMemory("NAT0",
                NAME_TABLE_0_START, ATTRIBUTE_TABLE_0_END,
                NAME_TABLE_0_SIZE + ATTRIBUTE_TABLE_0_SIZE)

        def bus = new TestBus(nameTable0)

        bus.write(0x2000, 0xAA)
        bus.write(0x201F, 0xBB)
        bus.write(0x23A0, 0xCC)
        bus.write(0x23BF, 0xDD)

        NameTable nameTable = new NameTable("NT0", segment, bus)

        expect:
        nameTable.getBackground( 0,  0) == ubyte(0xAA)
        nameTable.getBackground( 0, 31) == ubyte(0xBB)
        nameTable.getBackground(29,  0) == ubyte(0xCC)
        nameTable.getBackground(29, 31) == ubyte(0xDD)

        // println nameTable.printBackground()
    }
}
