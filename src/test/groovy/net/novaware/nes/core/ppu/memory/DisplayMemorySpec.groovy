package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.board.inject.BoardMemModule
import spock.lang.Specification

import static net.novaware.nes.core.config.CoreConfigBuilder.ntsc
import static net.novaware.nes.core.util.UTypes.ubyte

class DisplayMemorySpec extends Specification {

    def "should construct an instance with name and dimensions"() {
        given:
        def instance = new DisplayMemory("test", 9, 16)

        expect:
        instance.getName() == "test"
        instance.getWidth() == 16
        instance.getHeight() == 9
        instance.toString() == "test: 16x9"
    }

    // TODO: add tests for border region pixels
    def "should store and load color values"() {
        given:
        def instance = BoardMemModule.provideDisplayMemory(ntsc().build())

        when:
        instance.setColor(y, x, ubyte(colorIn))
        instance.swap()

        then:
        instance.getColor(y, x) == ubyte(colorOut)

        where:
        y   | x   | colorIn || colorOut
        0   | 0   | 0x0F    || 0x0F
        120 | 128 | 0xCD    || 0x0D
        239 | 255 | 0xAB    || 0x2B
    }
}
