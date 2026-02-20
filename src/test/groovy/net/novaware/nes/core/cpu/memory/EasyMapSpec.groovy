package net.novaware.nes.core.cpu.memory

import spock.lang.Specification

import static net.novaware.nes.core.easy.EasyMap.*
import static net.novaware.nes.core.util.UTypes.sint

class EasyMapSpec extends Specification {

    def "should cross check sizes" () {
        expect:
        MEMORY_SIZE == 0x10000
        RAM_SIZE == 0xFE
        STACK_SEGMENT_SIZE == 0x100
        PICTURE_SEGMENT_SIZE == 0x400
        CARTRIDGE_SIZE == 0xF9FA
        VECTOR_SEGMENT_SIZE == 0x6

        MEMORY_SIZE == RAM_SIZE +
            1 + // RNG
            1 + // KEY
            STACK_SEGMENT_SIZE +
            PICTURE_SEGMENT_SIZE +
            CARTRIDGE_SIZE +
            VECTOR_SEGMENT_SIZE
    }

    def "should cross check continuity" () {
        expect:
        sint(MEMORY_START)                 == sint(RAM_START)
        sint(RAM_END) + 1                  == sint(RNG_BYTE)
        sint(RNG_BYTE) + 1                 == sint(KEY_BYTE)
        sint(KEY_BYTE) + 1                 == sint(STACK_SEGMENT_START)
        sint(STACK_SEGMENT_END) + 1        == sint(PICTURE_SEGMENT_START)
        sint(PICTURE_SEGMENT_END) + 1      == sint(CARTRIDGE_START)
        sint(CARTRIDGE_END) + 1            == sint(VECTOR_SEGMENT_START)
        sint(VECTOR_SEGMENT_END)           == sint(MEMORY_END)
    }
}
