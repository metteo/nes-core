package net.novaware.nes.core.ppu.memory

import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte

class ExtBusSpec extends Specification {

    def "should construct an instance"() {
        when:
        def instance = new ExtBus()

        then:
        instance.toString() == "EXT 0b0000"
    }

    def "should read and write values"() {
        given:
        def ext = new ExtBus()

        when:
        ext.write(ubyte(data))

        then:
        ext.read() == ubyte(data)
        ext.toString() == toString

        where:
        data   | toString
        0b0000 | "EXT 0b0000"
        0b1010 | "EXT 0b1010"
        0b1111 | "EXT 0b1111"
    }

}
