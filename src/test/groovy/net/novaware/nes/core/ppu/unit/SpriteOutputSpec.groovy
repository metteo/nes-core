package net.novaware.nes.core.ppu.unit

import net.novaware.nes.core.util.Bin
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.UBYTE_0
import static net.novaware.nes.core.util.UTypes.ubyte

class SpriteOutputSpec extends Specification {

    def "should construct an instance"() {
        when:
        def instance = new SpriteOutput()

        then:
        instance != null
    }

    def "should load sprite into fresh unit"() {
        given:
        def spriteOutput = new SpriteOutput()

        when:
        def beforeLoad = spriteOutput.getDot(ubyte(xOut))
        spriteOutput.loadLine(ubyte(xIn), ubyte(patternHi), ubyte(patternLo), pal, hid, num)
        def afterLoad = spriteOutput.getDot(ubyte(xOut))

        then:
        beforeLoad == UBYTE_0
        Bin.s(afterLoad) == Bin.s(ubyte(result))

        where:
        xIn | patternHi   | patternLo   | pal  | hid   | num || xOut | result      | comment
        0   | 0b0000_0000 | 0b0000_0000 | 0    | false | 1   || 0    | 0b0000_0000 | "x=0, not dirty"
        0   | 0b0000_0000 | 0b1000_0000 | 0    | false | 1   || 0    | 0b1000_0001 | "x=0, patLo 7"
        0   | 0b1000_0000 | 0b0000_0000 | 0    | false | 1   || 0    | 0b1000_0010 | "x=0, patHi 7"
        0   | 0b1000_0000 | 0b0000_0000 | 0b11 | false | 1   || 0    | 0b1000_1110 | "x=0, pal with patHi"
        0   | 0b0000_0000 | 0b1000_0000 | 0b11 | false | 1   || 0    | 0b1000_1101 | "x=0, pal with patLo"
        0   | 0b1000_0000 | 0b0000_0000 | 0    | true  | 1   || 0    | 0b1001_0010 | "x=0, hid with patHi"
        0   | 0b0000_0000 | 0b1000_0000 | 0    | true  | 1   || 0    | 0b1001_0001 | "x=0, hid with patLo"
        0   | 0b1000_0000 | 0b0000_0000 | 0    | false | 0   || 0    | 0b1010_0010 | "x=0, s0  with patHi"
        0   | 0b0000_0000 | 0b1000_0000 | 0    | false | 0   || 0    | 0b1010_0001 | "x=0, s0  with patLo"
        0   | 0b1000_0000 | 0b0000_0000 | 0    | false | 8   || 0    | 0b1100_0010 | "x=0, sov with patHi"
        0   | 0b0000_0000 | 0b1000_0000 | 0    | false | 8   || 0    | 0b1100_0001 | "x=0, sov with patLo"

        0   | 0b0100_0000 | 0b0000_0000 | 0    | false | 1   || 1    | 0b1000_0010 | "x=1, dirty"
        0   | 0b0000_0000 | 0b0000_0001 | 0    | false | 1   || 7    | 0b1000_0001 | "x=7, dirty"
        0   | 0b0100_0000 | 0b0000_0000 | 0b11 | false | 1   || 1    | 0b1000_1110 | "x=1, pal"
        0   | 0b0000_0000 | 0b0000_0001 | 0b11 | false | 1   || 7    | 0b1000_1101 | "x=7, pal"
        0   | 0b0100_0000 | 0b0000_0000 | 0    | true  | 1   || 1    | 0b1001_0010 | "x=1, hid"
        0   | 0b0000_0000 | 0b0000_0001 | 0    | true  | 1   || 7    | 0b1001_0001 | "x=7, hid"
        0   | 0b0100_0000 | 0b0000_0000 | 0    | false | 0   || 1    | 0b1010_0010 | "x=1, s0"
        0   | 0b0000_0000 | 0b0000_0001 | 0    | false | 0   || 7    | 0b1010_0001 | "x=7, s0"
        0   | 0b0100_0000 | 0b0000_0000 | 0    | false | 9   || 1    | 0b1100_0010 | "x=1, sov"
        0   | 0b0000_0000 | 0b0000_0001 | 0    | false | 9   || 7    | 0b1100_0001 | "x=7, sov"

        0   | 0b0000_0000 | 0b0000_0001 | 0    | false | 1   || 7    | 0b1000_0001 | "x=7, patLo 0"
        0   | 0b0000_0001 | 0b0000_0000 | 0    | false | 1   || 7    | 0b1000_0010 | "x=7, patHi 0"

        248 | 0b0000_0000 | 0b0000_0001 | 0    | false | 1   || 255  | 0b1000_0001 | "x=255, patLo 0, last dot, last bit"
        255 | 0b0000_0000 | 0b0000_0001 | 0    | false | 1   || 6    | 0b1000_0001 | "x=255, patLo 0, wrap"
    }
}
