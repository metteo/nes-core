package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.ppu.inject.PpuMemModule
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte

class ObjAttrMemorySpec extends Specification {

    def "should construct instance"() {
        given:
        def instance = new ObjAttrMemory("OAM3")

        expect:
        instance.getName() == "OAM3"
    }

    def "should read and write using CPU Bus addressing"() {
        given:
        def oam = PpuMemModule.provideObjAttrMemory()

        when:
        oam.write(ubyte(addr), ubyte(data))

        then:
        oam.read(ubyte(addr)) == ubyte(data)

        where:
        addr | data
        0x00 | 0x12
        0x80 | 0x34
        0xFF | 0x56
    }

    def "should read and write using PPU index addressing"() {
        // TODO: implement when implementing sprite rendering
    }
}
