package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.inject.PpuRegModule
import net.novaware.nes.core.ppu.register.ViewPortRegister
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.*

class AttributeTablesSpec extends Specification {

    def v = PpuRegModule.provideCurrentViewPort()
    def segment = PpuMemModule.provideAttributeTablesSegment()
    MemoryBus bus = Mock()

    def "should return attribute table address (using view port)"() {
        given:
        def tables = new AttributeTables("ATS", segment, bus)

        v.setLayoutTable(lt)
        v.setCoarseX(coarseX)
        v.setCoarseY(coarseY)


        expect:
        tables.getAddress(v) == ushort(atAddr)

        where:
        lt   | coarseY  | coarseX  || atAddr
        0b00 | 0b000_00 | 0b000_00 || 0b10_00_1111_000_000
        0b00 | 0b000_00 | 0b111_00 || 0b10_00_1111_000_111
        0b00 | 0b111_00 | 0b000_00 || 0b10_00_1111_111_000
        0b01 | 0b000_00 | 0b000_00 || 0b10_01_1111_000_000
        0b10 | 0b000_00 | 0b000_00 || 0b10_10_1111_000_000
        0b11 | 0b111_00 | 0b111_00 || 0b10_11_1111_111_111
    }

    def "should return attribute address (using indexes)"() {
        given:
        int offset = 0x2000

        expect:
        AttributeTables.getAddress(offset, memRow, memCol, row, col) == addr

        where:
        memRow | memCol | attr | row   | col   || addr                 | comment
        0b0    | 0b0    | _    | 0b000 | 0b000 || 0b10_00_1111_000_000 | "zeros"
        0b0    | 0b0    | _    | 0b000 | 0b111 || 0b10_00_1111_000_111 | "col"
        0b0    | 0b0    | _    | 0b111 | 0b000 || 0b10_00_1111_111_000 | "row"
        0b0    | 0b1    | _    | 0b000 | 0b000 || 0b10_01_1111_000_000 | "memCol"
        0b1    | 0b0    | _    | 0b000 | 0b000 || 0b10_10_1111_000_000 | "memRow"
        0b1    | 0b1    | _    | 0b111 | 0b111 || 0b10_11_1111_111_111 | "ones"
    }

    def "should return sub attribute value"() {
        given:
        ViewPortRegister current = PpuRegModule.provideCurrentViewPort()
        current.setCoarseY(y)
        current.setCoarseX(x)

        when:
        def subAttribute = AttributeTables.subAttribute(ubyte(attribute), current)

        then:
        sint(subAttribute) == data

        where:
        attribute     | y          | x         | data | comment
        0b11_10_01_00 | 0b000_0_0  | 0b000_0_0 | 0b00 | "TL"
        0b11_10_01_00 | 0b000_0_0  | 0b000_1_0 | 0b01 | "TR"
        0b11_10_01_00 | 0b000_1_0  | 0b000_0_0 | 0b10 | "BL"
        0b11_10_01_00 | 0b000_1_0  | 0b000_1_0 | 0b11 | "BR"
    }

    def "should calculate subattribute mask"() {
        expect:
        AttributeTables.subMask(y, x) == mask

        where:
        y | x | mask
        //      0bBR_BL_TR_TL
        0 | 0 | 0b00_00_00_11
        0 | 1 | 0b00_00_11_00
        1 | 0 | 0b00_11_00_00
        1 | 1 | 0b11_00_00_00
    }
}
