package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ushort

class LayoutTablesSpec extends Specification {

    def v = PpuRegModule.provideCurrentViewPort()

    def "should return layout table address (using view port)"() {
        given:
        v.setLayoutTable(lt)
        v.setCoarseX(coarseX)
        v.setCoarseY(coarseY)

        expect:
        LayoutTables.getAddress(v) == ushort(ltAddr)

        where:
        lt   | coarseY | coarseX || ltAddr
        0b00 | 0b00000 | 0b00000 || 0b10_00_00000_00000
        0b00 | 0b00000 | 0b11111 || 0b10_00_00000_11111
        0b00 | 0b11111 | 0b00000 || 0b10_00_11111_00000
        0b11 | 0b00000 | 0b00000 || 0b10_11_00000_00000
        0b11 | 0b11111 | 0b11111 || 0b10_11_11111_11111 // y (30-31) technically is attribute table
    }

    def "should return layout table address (using indexes)"() {
        given:
        int offset = 0x2000 // TODO: maybe should be from segment register

        expect:
        LayoutTables.getAddress(offset, memRow, memCol, row, col) == addr

        where:
        memRow | memCol | row     | col     || addr
        0b0    | 0b0    | 0b00000 | 0b00000 || 0b10_00_00000_00000
        0b0    | 0b0    | 0b00000 | 0b11111 || 0b10_00_00000_11111
        0b0    | 0b0    | 0b11101 | 0b00000 || 0b10_00_11101_00000
        0b1    | 0b1    | 0b00000 | 0b00000 || 0b10_11_00000_00000
        0b1    | 0b1    | 0b11101 | 0b11111 || 0b10_11_11101_11111
    }

    def "should handle table rows out of range"() {
        expect:
        LayoutTables.getAddress(0x2000, 0, 0, row, 0) == addr

        // TODO: maybe check there was a warning?

        where:
        row      || addr                | comment
        0b11110  || 0b10_00_11110_00000 | "row 30"
        0b11111  || 0b10_00_11111_00000 | "row 31"
    }
}
