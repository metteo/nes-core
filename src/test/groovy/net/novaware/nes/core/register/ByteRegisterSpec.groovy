package net.novaware.nes.core.register

import spock.lang.Specification
import static net.novaware.nes.core.util.UTypes.ubyte

class ByteRegisterSpec extends Specification {

    def "should hold and return name"() {
        expect:
        new ByteRegister("X").getName() == "X"
    }

    def "should set and get unsigned byte data"() {
        given:
        def reg = new ByteRegister("A")

        when:
        reg.set(ubyte(0xFE))

        then:
        reg.get() == ubyte(0xFE)
    }

    def "should return data as unsigned int"() {
        given:
        def reg = new ByteRegister("A")

        when:
        reg.set(ubyte(0xFF))

        then:
        reg.getAsInt() == 255
    }

    def "should set data using int value"() {
        given:
        def reg = new ByteRegister("A")

        when:
        reg.setAsByte(0x80)

        then:
        reg.getAsInt() == 128
        reg.get() == ubyte(0x80)
    }
}
