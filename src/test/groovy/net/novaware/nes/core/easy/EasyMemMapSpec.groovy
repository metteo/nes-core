package net.novaware.nes.core.easy

import spock.lang.Specification

import static net.novaware.nes.core.easy.memory.EasyMemMap.*
import static net.novaware.nes.core.util.UTypes.sint

class EasyMemMapSpec extends Specification {

    def "should cross check sizes" () {
        expect:
        MEMORY_SIZE == 0x10000
        RAM_SIZE == 0x100
        STACK_SEGMENT_SIZE == 0x100
        PICTURE_SEGMENT_SIZE == 0x400
        CARTRIDGE_SIZE == 0xFA00

        MEMORY_SIZE == RAM_SIZE +
            STACK_SEGMENT_SIZE +
            PICTURE_SEGMENT_SIZE +
            CARTRIDGE_SIZE
    }

    def "should cross check continuity" () {
        expect:
        sint(MEMORY_START)                 == sint(RAM_START)
        sint(RAM_END) + 1                  == sint(STACK_SEGMENT_START)
        sint(STACK_SEGMENT_END) + 1        == sint(PICTURE_SEGMENT_START)
        sint(PICTURE_SEGMENT_END) + 1      == sint(CARTRIDGE_START)
        sint(CARTRIDGE_END)                == sint(MEMORY_END)
    }
}
