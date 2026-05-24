package net.novaware.nes.core.util

import spock.lang.Specification

import static net.novaware.nes.core.util.Bin.s
import static UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class BinSpec extends Specification {

    def "should print binary representation of byte" () {
        expect:
        actual == s(ubyte(input))

        where:
        input       || actual
        0b1010_1001 || "0b1010_1001"
        0b0000_0001 || "0b0000_0001"
        0b0001_0000 || "0b0001_0000"

    }

    def "should print binary representation of short" () {
        expect:
        actual == s(ushort(input))

        where:
        input                 || actual
        0b1010_1001_0001_0000 || "0b1010_1001_0001_0000"
        0b0000_0000_0000_0001 || "0b0000_0000_0000_0001"
        0b1000_0000_0000_0000 || "0b1000_0000_0000_0000"
    }
}
