package net.novaware.nes.core.register

import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.*

class ByteShifterSpec extends Specification {

    def "should construct an instance"() {
        when:
        def instance = new ByteShifter("test")

        then:
        instance.getName() == "test"
        instance.toString() == "test.HI: 0b1111_1111, test.LO: 0b0000_0000"

        // high plane defaults to 1s, low to 0s during init and shifting
        instance.planeLow() == UBYTE_0
        instance.planeHigh() == UBYTE_MAX_VALUE
    }

    def "should allow setting hi and lo planes"() {
        given:
        def shifter = new ByteShifter("test")

        when:
        shifter.loadPlaneLow(ubyte(inPlaneLo))
        shifter.loadPlaneHigh(ubyte(inPlaneHi))

        then:
        shifter.planeLow() == ubyte(outPlaneLo)
        shifter.planeHigh() == ubyte(outPlaneHi)

        where:
        inPlaneLo  | inPlaneHi || outPlaneLo | outPlaneHi
        0x00       | 0x00      || 0x00       | 0x00
        0xFF       | 0x00      || 0xFF       | 0x00
        0x00       | 0xFF      || 0x00       | 0xFF
        0xFF       | 0xFF      || 0xFF       | 0xFF
    }

    def "should get MSBs from both planes"() {
        given:
        def shifter = new ByteShifter("test")

        shifter.loadPlaneHigh(ubyte(inPlaneHi))
        shifter.loadPlaneLow(ubyte(inPlaneLo))
        shifter.shiftPlanes(4)

        when:
        def bits = shifter.getBits(0)

        then:
        bits == ubyte(outBits)

        where:
        inPlaneHi | inPlaneLo || outBits
        0x00      | 0x00      || 0b00
        0x00      | 0x0F      || 0b01
        0x0F      | 0x00      || 0b10
        0x0F      | 0x0F      || 0b11
    }

    def "should get offset bits from both planes"() {
        given:
        def shifter = new ByteShifter("test")

        shifter.loadPlaneHigh(ubyte(0b0011_0000))
        shifter.loadPlaneLow (ubyte(0b0101_1111))

        when:
        def bits = shifter.getBits(offset)

        then:
        bits == ubyte(outBits)

        where:
        offset || outBits
        0      || 0b00
        1      || 0b01
        2      || 0b10
        3      || 0b11
        7      || 0b01
    }

    def "should shift planes and return their MSBs"() {
        given:
        def shifter = new ByteShifter("test")
        shifter.loadPlaneHigh(ubyte(0x00))
        shifter.loadPlaneLow (ubyte(0xFF))

        expect:
        8.times { // inverse load above
            assert shifter.getBits(0) == ubyte(0b01)
            shifter.shiftPlanes()
        }

        // defaults from shifting
        shifter.getBits(0) == ubyte(0b10)

        // whole register has defaults
        shifter.planeLow() == UBYTE_0
        shifter.planeHigh() == UBYTE_MAX_VALUE
    }

    def "should throw AssertionError when getBits offset is out of bounds"() {
        given:
        def shifter = new ByteShifter("test")

        when:
        shifter.getBits(invalidOffset)

        then:
        def e = thrown(AssertionError)
        e.message == "offset out of range"

        where:
        invalidOffset << [-1, 8, 100]
    }

    // TODO: add missing tests:
    //  - shifting by more than 1 bit
}
