package net.novaware.nes.core.ppu.memory

import spock.lang.Specification

import static PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.sint

class PpuMemMapSpec extends Specification {

    def "should cross check sizes" () {
        expect:
        MEMORY_SIZE == 0x4000
        PATTERN_TABLE_0_SIZE == 0x1000
        PATTERN_TABLE_1_SIZE == 0x1000
        VRAM_SIZE == 0x1000
        NAME_TABLE_0_SIZE == 0x03C0
        NAME_TABLE_1_SIZE == 0x03C0
        NAME_TABLE_2_SIZE == 0x03C0
        NAME_TABLE_3_SIZE == 0x03C0
        ATTRIBUTE_TABLE_0_SIZE == 0x0040
        ATTRIBUTE_TABLE_1_SIZE == 0x0040
        ATTRIBUTE_TABLE_2_SIZE == 0x0040
        ATTRIBUTE_TABLE_3_SIZE == 0x0040
        UNUSED_SIZE == 0x0F00
        PALETTE_RAM_SIZE == 0x20
        PALETTE_RAM_MIRROR_SIZE == 0x0100

        MEMORY_SIZE == PATTERN_TABLE_0_SIZE +
                PATTERN_TABLE_1_SIZE +
                VRAM_SIZE +
                UNUSED_SIZE +
                PALETTE_RAM_MIRROR_SIZE

        VRAM_SIZE == NAME_TABLE_0_SIZE +
                NAME_TABLE_1_SIZE +
                NAME_TABLE_2_SIZE +
                NAME_TABLE_3_SIZE +
                ATTRIBUTE_TABLE_0_SIZE +
                ATTRIBUTE_TABLE_1_SIZE +
                ATTRIBUTE_TABLE_2_SIZE +
                ATTRIBUTE_TABLE_3_SIZE
    }

    def "should cross check continuity" () {
        expect:
        sint(MEMORY_START)            == sint(PATTERN_TABLE_0_START)
        sint(PATTERN_TABLE_0_END) + 1 == sint(PATTERN_TABLE_1_START)
        sint(PATTERN_TABLE_1_END) + 1 == sint(VRAM_START)
        sint(VRAM_END) + 1            == sint(UNUSED_START)
        sint(UNUSED_END) + 1          == sint(PALETTE_RAM_START)
        sint(PALETTE_RAM_MIRROR_END)  == sint(MEMORY_END)

        sint(VRAM_START)                == sint(NAME_TABLE_0_START)
        sint(NAME_TABLE_0_END) + 1      == sint(ATTRIBUTE_TABLE_0_START)
        sint(ATTRIBUTE_TABLE_0_END) + 1 == sint(NAME_TABLE_1_START)
        sint(NAME_TABLE_1_END) + 1      == sint(ATTRIBUTE_TABLE_1_START)
        sint(ATTRIBUTE_TABLE_1_END) + 1 == sint(NAME_TABLE_2_START)
        sint(NAME_TABLE_2_END) + 1      == sint(ATTRIBUTE_TABLE_2_START)
        sint(ATTRIBUTE_TABLE_2_END) + 1 == sint(NAME_TABLE_3_START)
        sint(NAME_TABLE_3_END) + 1      == sint(ATTRIBUTE_TABLE_3_START)
        sint(ATTRIBUTE_TABLE_3_END)     == sint(VRAM_END)
    }
}
