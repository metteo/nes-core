package net.novaware.nes.core.cpu.memory

import spock.lang.Specification

import static CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.sint

class CpuMemMapSpec extends Specification {

    def "should cross check sizes" () {
        expect:
        MEMORY_SIZE == 0x10000
        RAM_SIZE == 0x800
        RAM_MIRROR_SIZE == 0x2000
        ZERO_PAGE_SIZE == 0x100
        STACK_SEGMENT_SIZE == 0x100
        OAM_SEGMENT_SIZE == 0x100
        PPU_REGISTERS_SIZE == 0x8
        PPU_REGISTERS_MIRROR_SIZE == 0x2000
        APU_REGISTERS_SIZE == 0x14
        IO_REGISTERS_SIZE == 0x4
        APU_TEST_REGISTERS_SIZE == 0x4
        TIMER_REGISTERS_SIZE == 0x4
        CARTRIDGE_FDS_SIZE == 0x00E0
        CARTRIDGE_SIZE == 0xBF00
        CARTRIDGE_RAM_SIZE == 0x2000
        CARTRIDGE_ROM_SIZE == 0x8000

        MEMORY_SIZE == RAM_MIRROR_SIZE +
                PPU_REGISTERS_MIRROR_SIZE +
                APU_REGISTERS_SIZE +
                IO_REGISTERS_SIZE +
                APU_TEST_REGISTERS_SIZE +
                TIMER_REGISTERS_SIZE +
                CARTRIDGE_FDS_SIZE +
                CARTRIDGE_SIZE
    }

    def "should cross check continuity" () {
        expect:
        sint(MEMORY_START)                 == sint(RAM_START)
        sint(RAM_MIRROR_END) + 1           == sint(PPU_REGISTERS_START)
        sint(PPU_REGISTERS_MIRROR_END) + 1 == sint(APU_REGISTERS_START)
        sint(APU_REGISTERS_END) + 1        == sint(IO_REGISTERS_START)
        sint(IO_REGISTERS_END) + 1         == sint(APU_TEST_REGISTERS_START)
        sint(APU_TEST_REGISTERS_END) + 1   == sint(TIMER_REGISTERS_START)
        sint(TIMER_REGISTERS_END) + 1      == sint(CARTRIDGE_FDS_START)
        sint(CARTRIDGE_FDS_END) + 1        == sint(CARTRIDGE_START)
        sint(CARTRIDGE_END)                == sint(MEMORY_END)
    }
}
