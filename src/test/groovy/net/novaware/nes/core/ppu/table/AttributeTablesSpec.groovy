package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ushort

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
}
