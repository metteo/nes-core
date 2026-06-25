package net.novaware.nes.core.register

import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.UBYTE_0
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE
import static net.novaware.nes.core.util.UTypes.USHORT_0
import static net.novaware.nes.core.util.UTypes.USHORT_MAX_VALUE
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class ShortShifterSpec extends Specification {

    def "should construct an instance"() {
        when:
        def instance = new ShortShifter("test")

        then:
        instance.getName() == "test"
        instance.toString() == "test.HI: 0b1111_1111_1111_1111, test.LO: 0b0000_0000_0000_0000"

        // high plane defaults to 1s, low to 0s during init and shifting
        instance.planeLow() == USHORT_0
        instance.planeHigh() == USHORT_MAX_VALUE
    }

    def "should allow setting hi and lo planes"() {
        given:
        def shifter = new ShortShifter("test")

        when:
        shifter.loadPlaneLow(ubyte(inPlaneLo))
        shifter.loadPlaneHigh(ubyte(inPlaneHi))

        then:
        shifter.planeLow() == ushort(outPlaneLo)
        shifter.planeHigh() == ushort(outPlaneHi)

        where:
        inPlaneLo  | inPlaneHi || outPlaneLo | outPlaneHi
        0x00       | 0x00      || 0x00_00    | 0xFF_00
        0xFF       | 0x00      || 0x00_FF    | 0xFF_00
        0x00       | 0xFF      || 0x00_00    | 0xFF_FF
        0xFF       | 0xFF      || 0x00_FF    | 0xFF_FF
    }

    def "should get MSBs from both planes"() {
        given:
        def shifter = new ShortShifter("test")

        shifter.loadPlaneHigh(ubyte(inPlaneHi))
        shifter.loadPlaneLow(ubyte(inPlaneLo))
        shifter.shiftPlanes(8)

        when:
        def bits = shifter.getBits(0)

        then:
        bits == ubyte(outBits)

        where:
        inPlaneHi | inPlaneLo || outBits
        0x00      | 0x00      || 0b00
        0x00      | 0xFF      || 0b01
        0xFF      | 0x00      || 0b10
        0xFF      | 0xFF      || 0b11
    }

    def "should get offset bits from both planes"() {
        given:
        def shifter = new ShortShifter("test")

        shifter.loadPlaneHigh(ubyte(0b0011_0000))
        shifter.loadPlaneLow (ubyte(0b0101_1111))
        shifter.shiftPlanes(8)

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
        def shifter = new ShortShifter("test")
        shifter.loadPlaneHigh(ubyte(0x00))
        shifter.loadPlaneLow (ubyte(0xFF))

        expect:
        8.times { // defaults from init
            assert shifter.getBits(0) == ubyte(0b10)
            shifter.shiftPlanes()
        }

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
        def shifter = new ShortShifter("test")

        when:
        shifter.getBits(invalidOffset)

        then:
        def e = thrown(AssertionError)
        e.message == "offset out of range"

        where:
        invalidOffset << [-1, 8, 100]
    }

    // TODO: add missing tests:
    //  - preserve non default upper byte data when loading a subsequent lower byte
    //  - shifting by more than 1 bit

}
