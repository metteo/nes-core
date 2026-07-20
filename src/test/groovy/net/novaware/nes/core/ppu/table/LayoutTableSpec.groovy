package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.memory.PpuBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

// TODO: create a base Spec for testing Table/s
class LayoutTableSpec extends Specification {

    def layoutAttrTable0 = new PhysicalMemory("LAT0",
            LAYOUT_TABLE_0_START, ATTRIBUTE_TABLE_0_END,
            LAYOUT_TABLE_0_SIZE + ATTRIBUTE_TABLE_0_SIZE)
    def segment = PpuMemModule.provideLayoutTable0Segment()

    def stringWriter = new StringWriter()
    def printWriter = new PrintWriter(stringWriter)

    def "should construct an instance"() {
        given:
        PpuBus bus = Mock()

        when:
        def instance = new LayoutTable("test", segment, bus)

        then:
        instance.getName() == "test"
        instance.toString() == "test (2000:23BF)"
    }

    def "should properly construct layout table address"() {
        given:
        PpuBus bus = Mock()
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
        def bus = new PpuBus()
        bus.attachCartridge(layoutAttrTable0)

        bus.access(ushort(0x2000)).write().data(ubyte(0xAA))
        bus.access(ushort(0x201F)).write().data(ubyte(0xBB))
        bus.access(ushort(0x23A0)).write().data(ubyte(0xCC))
        bus.access(ushort(0x23BF)).write().data(ubyte(0xDD))

        LayoutTable layoutTable = new LayoutTable("LT0", segment, bus)
        def printer = new LayoutPrinter(layoutTable, printWriter)

        expect:
        layoutTable.getPatternRef( 0,  0) == ubyte(0xAA)
        layoutTable.getPatternRef( 0, 31) == ubyte(0xBB)
        layoutTable.getPatternRef(29,  0) == ubyte(0xCC)
        layoutTable.getPatternRef(29, 31) == ubyte(0xDD)

        // printer.printAll(); println stringWriter.toString()
    }
}
