package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.ppu.inject.PpuMemModule
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.SECONDARY_ENTRY_COUNT
import static net.novaware.nes.core.util.UTypes.ubyte

class ObjAttrMemorySpec extends Specification {

    def "should construct an instance"() {
        given:
        def instance = new ObjAttrMemory("OAM3", SECONDARY_ENTRY_COUNT)

        expect:
        instance.getName() == "OAM3"
        instance.toString() == "OAM3 (00:FF)"
    }

    def "should read and write using PPU Primary OAM addressing"() {
        given:
        def oam = PpuMemModule.provideObjAttrMemory()

        when:
        oam.writePrimary(ubyte(addr), ubyte(data))

        then:
        oam.readPrimary(ubyte(addr)) == ubyte(data)

        where:
        addr | data
        0x00 | 0x12
        0x80 | 0x34
        0xFF | 0x56
    }

    def "should copy primary bytes into secondary entry"() {
        given:
        def oam = PpuMemModule.provideObjAttrMemory()
        oam.writePrimary(ubyte(addr + 0), ubyte(b0))
        oam.writePrimary(ubyte(addr + 1), ubyte(b1))
        oam.writePrimary(ubyte(addr + 2), ubyte(b2))
        oam.writePrimary(ubyte(addr + 3), ubyte(b3))

        def entry = oam.secondary.get(sI)

        when:
        oam.copyToSecondary(pI, sI)
        // println oam.printOam()

        then:
        entry.y == ubyte(b0)

        entry.tile == ubyte(b1)

        entry.flipH == flipH
        entry.flipV == flipV
        entry.hidden == hide
        entry.palette == ubyte(pal)
        entry.unused == ubyte(unused)

        entry.x == ubyte(b3)

        entry.primaryIndex == pI

        where:
        addr | b0   | b1   | b2   | b3   | pI | sI || flipV | flipH | hide  | unused | pal
        0x00 | 0x00 | 0x00 | 0x00 | 0x00 | 0  | 0  || false | false | false | 0b000  | 0b00
        0x00 | 0xFF | 0x00 | 0x00 | 0x00 | 0  | 0  || false | false | false | 0b000  | 0b00
        0x00 | 0x00 | 0xFF | 0x00 | 0x00 | 0  | 0  || false | false | false | 0b000  | 0b00
        0x00 | 0x00 | 0x00 | 0xFF | 0x00 | 0  | 0  || true  | true  | true  | 0b111  | 0b11
        0x00 | 0x00 | 0x00 | 0x00 | 0xFF | 0  | 0  || false | false | false | 0b000  | 0b00
        0x00 | 0x00 | 0x00 | 0x00 | 0x00 | 0  | 7  || false | false | false | 0b000  | 0b00
        0xFC | 0xFF | 0xFF | 0xFF | 0xFF | 63 | 7  || true  | true  | true  | 0b111  | 0b11
    }

    def "should decode byte2 into an entry"() {
        given:
        def entry = new ObjAttrMemory.ObjAttrEntry()

        when:
        ObjAttrMemory.decodeByte2(byte2, entry)

        then:
        entry.palette == ubyte(pal)
        entry.unused == ubyte(unused)
        entry.hidden == hide
        entry.flipH == flipH
        entry.flipV == flipV

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
