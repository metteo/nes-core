package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.ppu.inject.PpuMemModule
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.Kind.SECONDARY
import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.SECONDARY_ENTRY_COUNT
import static net.novaware.nes.core.util.UTypes.ubyte

class ObjAttrMemorySpec extends Specification {

    def "should construct an instance"() {
        given:
        def instance = new ObjAttrMemory("OAM3", SECONDARY, SECONDARY_ENTRY_COUNT)

        expect:
        instance.getName() == "OAM3"
        instance.getSize() == 32
        instance.getCount() == 8
        instance.toString() == "OAM3 (00:1F)"
    }

    def "should read and write using PPU addressing"() {
        given:
        def oam = PpuMemModule.providePrimaryObjAttrMemory()

        def d = ubyte(data)
        def a = ubyte(addr)

        when:
        oam.write(a, d)

        then:
        oam.read(a) == d

        where:
        addr | data
        0x00 | 0x12
        0x80 | 0x34
        0xFF | 0x56
    }

    def "should not store unused attribute bits in primary OAM"() {
        def oam = PpuMemModule.providePrimaryObjAttrMemory()

        when:
        oam.write(ubyte(addr), ubyte(0xFF))

        then:
        oam.read(ubyte(addr)) == ubyte(0b1110_0011)

        where:
        addr << [0x02, 0x06]
    }

    def "should wrap around with smaller secondary instance"() {
        given:
        def oam = PpuMemModule.provideSecondaryObjAttrMemory()

        def d = ubyte(data)
        def a = ubyte(addr)
        def aWrap = ubyte(addr + oam.getSize())

        when:
        oam.write(a, d)

        then:
        oam.read(aWrap) == d

        where:
        addr | data
        0x00 | 0x12
        0x0F | 0x34
        0x1F | 0x56
        0x20 | 0x78
    }
}
