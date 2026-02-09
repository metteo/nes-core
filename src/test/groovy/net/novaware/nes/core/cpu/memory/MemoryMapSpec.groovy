package net.novaware.nes.core.cpu.memory

import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.MemoryMap.*
import static net.novaware.nes.core.util.UnsignedTypes.uint

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
        uint(MEMORY_START)                 == uint(RAM_START)
        uint(RAM_END) + 1                  == uint(RAM_MIRROR_1_START)
        uint(RAM_MIRROR_1_END) + 1         == uint(RAM_MIRROR_2_START)
        uint(RAM_MIRROR_2_END) + 1         == uint(RAM_MIRROR_3_START)
        uint(RAM_MIRROR_3_END) + 1         == uint(PPU_REGISTERS_START)
        uint(PPU_REGISTERS_END) + 1        == uint(PPU_REGISTERS_MIRROR_START)
        uint(PPU_REGISTERS_MIRROR_END) + 1 == uint(APU_IO_REGISTERS_START)
        uint(APU_IO_REGISTERS_END) + 1     == uint(APU_TEST_REGISTERS_START)
        uint(APU_TEST_REGISTERS_END) + 1   == uint(CARTRIDGE_START)
        uint(CARTRIDGE_END)                == uint(MEMORY_END)
    }
}
