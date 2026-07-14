package net.novaware.nes.core.ppu.unit

import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte

class SpriteOutputSpec extends Specification {

    def "should construct an instance"() {
        when:
        def instance = new SpriteOutput()

        then:
        instance != null
    }

    def "should load priority at correct x location (aligned)"() {
        given:
        def spriteOutput = new SpriteOutput()

        Arrays.fill(spriteOutput.priority, ubyte(0b1010_1010)) // watermark

        when:
        spriteOutput.loadPriority(x, ubyte(priority))

        then:
        spriteOutput.priority[index] == ubyte(value)

        where:
          x | priority || index | value
          0 | 0        || 0     | 0b0000_0000
          0 | 1        || 0     | 0b1111_1111
        128 | 0        || 16    | 0b0000_0000
        128 | 1        || 16    | 0b1111_1111
        248 | 0        || 31    | 0b0000_0000
        248 | 1        || 31    | 0b1111_1111
    }

    def "should load priority at correct x location (split)"() {
        given:
        def spriteOutput = new SpriteOutput()

        Arrays.fill(spriteOutput.priority, ubyte(fill))

        when:
        spriteOutput.loadPriority(x, ubyte(priority))

        then:
        spriteOutput.priority[leftIdx] == ubyte(leftVal)
        spriteOutput.priority[rightIdx] == ubyte(rightVal)

        where:
        fill | x      | priority || leftIdx | leftVal     | rightIdx | rightVal    | comment
        0xFF | 1      | 0        || 0       | 0b1_0000000 | 1        | 0b0_1111111 | ""
        0x00 | 1      | 1        || 0       | 0b0_1111111 | 1        | 0b1_0000000 | ""

        0xFF | 2      | 0        || 0       | 0b11_000000 | 1        | 0b00_111111 | ""
        0x00 | 2      | 1        || 0       | 0b00_111111 | 1        | 0b11_000000 | ""

        0xFF | 3      | 0        || 0       | 0b111_00000 | 1        | 0b000_11111 | ""
        0x00 | 3      | 1        || 0       | 0b000_11111 | 1        | 0b111_00000 | ""

        0xFF | 7      | 0        || 0       | 0b1111111_0 | 1        | 0b0000000_1 | ""
        0x00 | 7      | 1        || 0       | 0b0000000_1 | 1        | 0b1111111_0 | ""

        0xFF |  1*8+3 | 0        || 1       | 0b111_00000 | 2        | 0b000_11111 | "cX=1,2"
        0x00 |  1*8+3 | 1        || 1       | 0b000_11111 | 2        | 0b111_00000 | "cX=1,2"

        0xFF | 30*8+3 | 0        || 30      | 0b111_00000 | 31       | 0b000_11111 | "cX=30,31"
        0x00 | 30*8+3 | 1        || 30      | 0b000_11111 | 31       | 0b111_00000 | "cX=30,31"
    }

    def "should get correct pattern bits"() {
        def spriteOutput = new SpriteOutput()

        spriteOutput.loadPatternHi(inX, ubyte(patternHi))
        spriteOutput.loadPatternLo(inX, ubyte(patternLo))

        when:
        def pattern = spriteOutput.getPattern(outX)

        then:
        pattern == ubyte(outPattern)

        where:
        inX | patternHi   | patternLo   || outX | outPattern
        0   | 0b0_1111111 | 0b0_1111111 || 0    | 0b00
        0   | 0b1_0000000 | 0b0_0000000 || 0    | 0b10
        0   | 0b0_0000000 | 0b1_0000000 || 0    | 0b01
        0   | 0b1_0000000 | 0b1_0000000 || 0    | 0b11

        3   | 0b0_1111111 | 0b0_1111111 || 3    | 0b00
        3   | 0b1_0000000 | 0b0_0000000 || 3    | 0b10
        3   | 0b0_0000000 | 0b1_0000000 || 3    | 0b01
        3   | 0b1_0000000 | 0b1_0000000 || 3    | 0b11
    }
}
