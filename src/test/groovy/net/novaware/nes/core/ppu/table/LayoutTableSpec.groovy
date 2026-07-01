package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.test.TestBus
import net.novaware.nes.core.util.LayoutPrinter
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte

class LayoutTableSpec extends Specification {

    def layoutAttrTable0 = new PhysicalMemory("LAT0",
            LAYOUT_TABLE_0_START, ATTRIBUTE_TABLE_0_END,
            LAYOUT_TABLE_0_SIZE + ATTRIBUTE_TABLE_0_SIZE)
    def segment = PpuMemModule.provideLayoutTable0Segment()

    def stringWriter = new StringWriter()
    def printWriter = new PrintWriter(stringWriter)

    def "should construct an instance"() {
        given:
        MemoryBus bus = Mock()

        when:
        def instance = new LayoutTable("test", segment, bus)

        then:
        instance.getName() == "test"
        instance.toString() == "test (2000:23BF)"
    }

    def "should properly construct layout table address"() {
        given:
        MemoryBus bus = Mock()
        def table = new LayoutTable("test", segment, bus)

        expect:
        table.getAddress(row, col) == addr

        where: "corners"
        row | col | addr
        0   | 0   | 0x2000
        0   | 31  | 0x201F
        29  | 0   | 0x23A0
        29  | 31  | 0x23BF
    }

    def "should properly read layout table corners"() {
        given:
        def bus = new TestBus(layoutAttrTable0)

        bus.write(0x2000, 0xAA)
        bus.write(0x201F, 0xBB)
        bus.write(0x23A0, 0xCC)
        bus.write(0x23BF, 0xDD)

        LayoutTable layoutTable = new LayoutTable("LT0", segment, bus)
        def printer = new LayoutPrinter(layoutTable, printWriter)

        expect:
        layoutTable.getPattern( 0,  0) == ubyte(0xAA)
        layoutTable.getPattern( 0, 31) == ubyte(0xBB)
        layoutTable.getPattern(29,  0) == ubyte(0xCC)
        layoutTable.getPattern(29, 31) == ubyte(0xDD)

        // printer.printAll(); println stringWriter.toString()
    }
}
