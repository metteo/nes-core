package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.inject.PpuRegModule
import spock.lang.Specification

import static net.novaware.nes.core.ppu.table.ObjAttrTable.asFlipH
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asFlipV
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asHidden
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asPalette
import static net.novaware.nes.core.ppu.table.ObjAttrTable.asUnused
import static net.novaware.nes.core.util.UTypes.ubyte

class ObjAttrTableSpec extends Specification {

    def priCursor = PpuRegModule.providePrimaryObjAttrAddress()
    def secCursor = PpuRegModule.provideSecondaryObjAttrAddress()
    def priMemory = PpuMemModule.providePrimaryObjAttrMemory()
    def secMemory = PpuMemModule.provideSecondaryObjAttrMemory()

    def "should construct an instance"() {
        when:
        def primary = newPrimaryOAT()
        def secondary = newSecondaryOAT()

        then:
        primary.getName() == "OAM.PRI"
        primary.toString() == "OAM.PRI (0:64)"

        secondary.getName() == "OAM.SEC"
        secondary.toString() == "OAM.SEC (0:8)"
    }

    def newPrimaryOAT() {
        new ObjAttrTable("OAM.PRI", priCursor, priMemory)
    }

    def newSecondaryOAT() {
        new ObjAttrTable("OAM.SEC", secCursor, secMemory)
    }

    def "should get columns of the current row"() {
        given:
        def primary = newPrimaryOAT()

        priMemory.write(ubyte(0), ubyte(0xAA))
        priMemory.write(ubyte(1), ubyte(0xBB))
        priMemory.write(ubyte(2), ubyte(0xCC))
        priMemory.write(ubyte(3), ubyte(0xDD))

        priCursor.set(ubyte(0))

        expect:
        primary.getY() == ubyte(0xAA)
        primary.getTile() == ubyte(0xBB)
        primary.getAttr() == ubyte(0xCC & ~0b11100) // 3 bits of primary oam are disabled
        primary.getX() == ubyte(0xDD)
    }

    def "should go to next row"() {
        given:
        def secondary = newSecondaryOAT()

        secCursor.set(ubyte(inAddr))

        when:
        secondary.nextRow()

        then:
        secCursor.getAsInt() == outAddr
        secondary.getRow() == outRow

        where:
        inAddr || outAddr | outRow
        0x00   || 0x04    | 1 // first
        0x10   || 0x14    | 5 // "middle"
        0x18   || 0x1C    | 7 // last
        0x1C   || 0x00    | 0 // wrap around
    }

    def "should detect misaligned access to rows"() {
        given:
        def secTable = newSecondaryOAT()

        secCursor.setAsByte(0x01)

        when:
        secTable.getRow()

        then:
        def e = thrown(IllegalStateException)
        e.message == "misaligned table access"
    }

    def "should write using PPU Secondary OAM addressing (full bytes)"() {
        given:
        def oat = newSecondaryOAT()

        when:
        secMemory.write(ubyte(address), ubyte(data))
        oat.setRow(row)

        then:
        oat[byteField] == ubyte(data)

        where:
        address | data || row | byteField | comment
        0x00    | 0xAB || 0   | "y"       | "1st byte"
        0x01    | 0xCD || 0   | "tile"    | "2nd byte"
        0x03    | 0xEF || 0   | "x"       | "4th byte"
        0x1F    | 0xFF || 7   | "x"       | "last byte"
    }

    def "should write using PPU Secondary OAM addressing (byte 2)"() {
        given:
        def oat = newSecondaryOAT()

        when:
        secMemory.write(ubyte(address), ubyte(data))
        oat.setRow(index)
        def attr = oat.getAttr()

        then:
        asFlipV(attr) == flipV
        asFlipH(attr) == flipH
        asHidden(attr) == hidden
        asUnused(attr) == ubyte(unused)
        asPalette(attr) == ubyte(pal)

        where:
        address | data         || index | flipV | flipH | hidden | unused | pal  | comment
        0x02    | 0b000_000_00 || 0     | false | false | false  | 0b000  | 0b00 | "none, first"
        0x06    | 0b100_000_00 || 1     | true  | false | false  | 0b000  | 0b00 | "flipV, second"
        0x06    | 0b010_000_00 || 1     | false | true  | false  | 0b000  | 0b00 | "flipH, second"
        0x06    | 0b001_000_00 || 1     | false | false | true   | 0b000  | 0b00 | "hidden, second"
        0x06    | 0b000_111_00 || 1     | false | false | false  | 0b111  | 0b00 | "unused, second"
        0x06    | 0b000_000_11 || 1     | false | false | false  | 0b000  | 0b11 | "palette, second"
        0x1E    | 0b111_111_11 || 7     | true  | true  | true   | 0b111  | 0b11 | "all bits, last"

    }

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
