package net.novaware.nes.core.cpu.memory

import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.MemoryMap.*
import static net.novaware.nes.core.util.UnsignedTypes.sint

class MemoryMapSpec extends Specification {

    def "should cross check sizes" () {
        expect:
        MEMORY_SIZE == 0x10000
        RAM_SIZE == 0x800
        STACK_SEGMENT_SIZE == 0x100
        RAM_MIRROR_1_SIZE == 0x800
        RAM_MIRROR_2_SIZE == 0x800
        RAM_MIRROR_3_SIZE == 0x800
        PPU_REGISTERS_SIZE == 0x8
        PPU_REGISTERS_MIRROR_SIZE == 0x1FF8
        APU_IO_REGISTERS_SIZE == 0x18
        APU_TEST_REGISTERS_SIZE == 0x8
        CARTRIDGE_SIZE == 0xBFE0

        MEMORY_SIZE == RAM_SIZE +
                RAM_MIRROR_1_SIZE +
                RAM_MIRROR_2_SIZE +
                RAM_MIRROR_3_SIZE +
                PPU_REGISTERS_SIZE +
                PPU_REGISTERS_MIRROR_SIZE +
                APU_IO_REGISTERS_SIZE +
                APU_TEST_REGISTERS_SIZE +
                CARTRIDGE_SIZE
    }

    def "should cross check continuity" () {
        expect:
        sint(MEMORY_START)                 == sint(RAM_START)
        sint(RAM_END) + 1                  == sint(RAM_MIRROR_1_START)
        sint(RAM_MIRROR_1_END) + 1         == sint(RAM_MIRROR_2_START)
        sint(RAM_MIRROR_2_END) + 1         == sint(RAM_MIRROR_3_START)
        sint(RAM_MIRROR_3_END) + 1         == sint(PPU_REGISTERS_START)
        sint(PPU_REGISTERS_END) + 1        == sint(PPU_REGISTERS_MIRROR_START)
        sint(PPU_REGISTERS_MIRROR_END) + 1 == sint(APU_IO_REGISTERS_START)
        sint(APU_IO_REGISTERS_END) + 1     == sint(APU_TEST_REGISTERS_START)
        sint(APU_TEST_REGISTERS_END) + 1   == sint(CARTRIDGE_START)
        sint(CARTRIDGE_END)                == sint(MEMORY_END)
    }
}
