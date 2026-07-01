package net.novaware.nes.core.util

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author gemini
 */
class BitSpec extends Specification {

    @Unroll
    def "factory method of(#input) should resolve to numeric value #expectedValue"() {
        expect:
        Bit.of(input).toInt() == expectedValue

        where:
        input | expectedValue
        0     | 0
        1     | 1
        2     | 0 // bitmask wrapping (2 & 1 = 0)
        3     | 1 // bitmask wrapping (3 & 1 = 1)
    }

    @Unroll
    def "inverting #start should yield #expected"() {
        expect:
        start.not() == expected

        where:
        start    | expected
        Bit.ZERO | Bit.ONE
        Bit.ONE  | Bit.ZERO
    }

    def "operations must maintain strict reference equality to eliminate GC"() {
        given:
        def bitZero = Bit.of(0)
        def bitOne = Bit.of(1)

        expect: "No new objects are generated on the heap during manipulation"
        bitZero.not() is Bit.ONE
        bitOne.not() is Bit.ZERO
        Bit.of(0) is Bit.ZERO
    }
}
