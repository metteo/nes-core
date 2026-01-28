package net.novaware.nes.core.ppu.memory

import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.MemoryMap.*
import static net.novaware.nes.core.util.UnsignedTypes.uint

class MemoryMapSpec extends Specification {

    def "should cross check sizes" () {
        expect:
        MEMORY_SIZE == 0x4000
        PATTERN_TABLE_1_SIZE == 0x1000
        PATTERN_TABLE_2_SIZE == 0x1000
        VRAM_SIZE == 0x1000
        UNUSED_SIZE == 0x0F00
        PALETTE_RAM_SIZE == 0x20
        PALETTE_RAM_MIRROR_SIZE == 0x00E0

        MEMORY_SIZE == PATTERN_TABLE_1_SIZE +
                PATTERN_TABLE_2_SIZE +
                VRAM_SIZE +
                UNUSED_SIZE +
                PALETTE_RAM_SIZE +
                PALETTE_RAM_MIRROR_SIZE
    }

    def "should cross check continuity" () {
        expect:
        uint(MEMORY_START)            == uint(PATTERN_TABLE_1_START)
        uint(PATTERN_TABLE_1_END) + 1 == uint(PATTERN_TABLE_2_START)
        uint(PATTERN_TABLE_2_END) + 1 == uint(VRAM_START)
        uint(VRAM_END) + 1            == uint(UNUSED_START)
        uint(UNUSED_END) + 1          == uint(PALETTE_RAM_START)
        uint(PALETTE_RAM_END) + 1     == uint(PALETTE_RAM_MIRROR_START)
        uint(PALETTE_RAM_MIRROR_END)  == uint(MEMORY_END)
    }
}
