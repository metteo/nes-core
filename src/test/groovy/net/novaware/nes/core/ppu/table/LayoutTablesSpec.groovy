package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ushort

class LayoutTablesSpec extends Specification {

    def v = PpuRegModule.provideCurrentViewPort()

    def "should return layout table address"() {
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
}
