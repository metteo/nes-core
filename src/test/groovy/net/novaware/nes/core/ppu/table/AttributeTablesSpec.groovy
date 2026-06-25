package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ushort

class AttributeTablesSpec extends Specification {

    def v = PpuRegModule.provideCurrentViewPort()

    def "should return attr table address"() {
        given:
        v.setNameTable(nt)
        v.setCoarseX(coarseX)
        v.setCoarseY(coarseY)


        expect:
        AttributeTables.getAttrTableAddress(v) == ushort(atAddr)

        where:
        nt   | coarseY  | coarseX  || atAddr
        0b00 | 0b000_00 | 0b000_00 || 0b10_00_1111_000_000
        0b00 | 0b000_00 | 0b111_00 || 0b10_00_1111_000_111
        0b00 | 0b111_00 | 0b000_00 || 0b10_00_1111_111_000
        0b11 | 0b000_00 | 0b000_00 || 0b10_11_1111_000_000
        0b11 | 0b111_00 | 0b111_00 || 0b10_11_1111_111_111
    }
}
