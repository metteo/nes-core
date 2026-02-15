package net.novaware.nes.core.register

import spock.lang.Specification

import static net.novaware.nes.core.util.UnsignedTypes.ubyte
import static net.novaware.nes.core.util.UnsignedTypes.ushort

class ShortRegisterSpec extends Specification {

    def "should hold and return name"() {
        expect:
        new ShortRegister("PC").getName() == "PC"
    }

    def "should set and get unsigned short address"() {
        given:
        def reg = new ShortRegister("PC")

        when:
        reg.set(ushort(0xC000))

        then:
        reg.get() == ushort(0xC000)
    }

    def "should return address as unsigned int"() {
        given:
        def reg = new ShortRegister("PC")

        when:
        reg.set(ushort(0xFFFF))

        then:
        reg.getAsInt() == 65535
    }

    def "should set address using int value"() {
        given:
        def reg = new ShortRegister("PC")

        when:
        reg.setAsShort(0x8000)

        then:
        reg.getAsInt() == 32768
        reg.get() == ushort(0x8000)
    }

    def "should provide access to high and low bytes"() {
        given:
        def reg = new ShortRegister("PC")

        when:
        reg.set(ushort(0x1234))

        then:
        reg.high() == ubyte(0x12)
        reg.low() == ubyte(0x34)
        reg.highAsInt() == 0x12
        reg.lowAsInt() == 0x34
    }

    def "should allow setting high and low bytes individually"() {
        given:
        def reg = new ShortRegister("PC")

        when:
        reg.high(ubyte(0xCA)).low(ubyte(0xFE))

        then:
        reg.get() == ushort(0xCAFE)
    }

    def "should allow setting high and low bytes using int values"() {
        given:
        def reg = new ShortRegister("PC")

        when:
        reg.highAsByte(0xAB).lowAsByte(0xCD)

        then:
        reg.getAsInt() == 0xABCD
    }
}
