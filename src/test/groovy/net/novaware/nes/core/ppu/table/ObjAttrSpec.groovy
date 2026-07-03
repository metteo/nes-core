package net.novaware.nes.core.ppu.table

import spock.lang.Specification

import static net.novaware.nes.core.ppu.table.ObjAttr.*
import static net.novaware.nes.core.util.UTypes.ubyte

class ObjAttrSpec extends Specification {

    def "should decode attr byte as separate attributes"() {
        given:
        def ub = ubyte(byte2)

        expect:
        asPalette(ub) == ubyte(pal)
        asUnused(ub) == ubyte(unused)
        asHidden(ub) == hide
        asFlipH(ub) == flipH
        asFlipV(ub) == flipV

        where:
        byte2       || flipV | flipH | hide  | unused | pal
        0b0000_0000 || false | false | false | 0b000  | 0b00
        0b1000_0000 || true  | false | false | 0b000  | 0b00
        0b0100_0000 || false | true  | false | 0b000  | 0b00
        0b0010_0000 || false | false | true  | 0b000  | 0b00
        0b0001_1100 || false | false | false | 0b111  | 0b00
        0b0000_0011 || false | false | false | 0b000  | 0b11
        0b1111_1111 || true  | true  | true  | 0b111  | 0b11
    }
}
