package net.novaware.nes.core.cpu.register

import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte

class StatusRegisterSpec extends Specification {

    def "should hold and return name"() {
        expect:
        new StatusRegister("P").getName() == "P"
    }

    def "should have correct power on state"() {
        given:
        def reg = new StatusRegister("P")

        when:
        reg.initialize()

        then:
        !reg.isNegative()
        !reg.isOverflow()
        !reg.isDecimal()
        reg.isIrqDisabled()
        !reg.isZero()
        !reg.getCarry()
        reg.get().getAsInt() == 0b0010_0100
    }

    def "should update IRQ flag on reset"() {
        given:
        def reg = new StatusRegister("P")
        reg.setIrqDisabled(false)

        when:
        reg.reset()

        then:
        reg.isIrqDisabled()
    }

    def "should set and get individual flags"() {
        given:
        def reg = new StatusRegister("P")

        expect:
        reg.setNegative(true).isNegative()
        reg.setOverflow(true).isOverflow()
        reg.setDecimal(true).isDecimal()
        reg.setIrqDisabled(true).isIrqDisabled()
        reg.setZero(true).isZero()
        reg.setCarry(true).getCarry()
    }

    def "should calculate aggregate status byte correctly"() {
        given:
        def reg = new StatusRegister("P")

        when: "all flags set"
        reg.setNegative(true)
           .setOverflow(true)
           .setDecimal(true)
           .setIrqDisabled(true)
           .setZero(true)
           .setCarry(true)

        then:
        reg.get().getAsInt() == 0xEF // break = 0
        reg.get().get() == ubyte(0xEF)

        when: "all flags cleared (except bit 5)"
        reg.setNegative(false)
           .setOverflow(false)
           .setDecimal(false)
           .setIrqDisabled(false)
           .setZero(false)
           .setCarry(false)

        then:
        reg.get().getAsInt() == 0x20
        reg.get().get() == ubyte(0x20)
    }

    def "updateZN sets flags correctly for 8-bit boundaries"() {
        given:
        def reg = new StatusRegister("P")

        when:
        reg.maybeZeroOrNegative(input)

        then:
        reg.isZero() == zero
        reg.isNegative() == neg

        where:
        input || zero  | neg   | description
        0x00  || true  | false | "Zero result"
        0x01  || false | false | "Small positive"
        0x7F  || false | false | "Max positive"
        0x80  || false | true  | "Smallest negative"
        0xFF  || false | true  | "Max negative (-1)"
        0x100 || true  | false | "Overflow to 0 (Carry set)"
        0x180 || false | true  | "Overflow to 0x80 (Carry set)"
    }
}
