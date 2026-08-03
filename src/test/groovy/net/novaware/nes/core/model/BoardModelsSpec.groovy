package net.novaware.nes.core.model

import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class BoardModelsSpec extends Specification {

    static final double NTSC_CPU_FREQ  = 1_789_772.7d
    static final double DENDY_CPU_FREQ = 1_773_447.5d

    static final double PAL_PPU_FREQ   = 5_320_342.50d
    static final double NTSC_PPU_FREQ  = 5_369_318.18d

    def "should calculate constants correctly"() {
        expect:
        assertThat(board.cpuFrequency(), closeTo(cpuFreq, 0.1d))
        assertThat(board.ppuFrequency(), closeTo(ppuFreq, 0.01d))

        where:
        board                      | cpuFreq        | ppuFreq
        BoardModels.NES            | NTSC_CPU_FREQ  | NTSC_PPU_FREQ
        BoardModels.NES_RGB        | NTSC_CPU_FREQ  | NTSC_PPU_FREQ
        BoardModels.FAMICOM        | NTSC_CPU_FREQ  | NTSC_PPU_FREQ
        BoardModels.NES_EU         | 1_662_607.0d   | PAL_PPU_FREQ
        BoardModels.DENDY          | DENDY_CPU_FREQ | PAL_PPU_FREQ
        BoardModels.PEGASUS        | DENDY_CPU_FREQ | PAL_PPU_FREQ
        BoardModels.PHANTOM_SYSTEM | 1_787_805.9d   | 5_363_417.83d
        BoardModels.SUPER_BITGAME  | 1_791_028.1d   | 5_373_084.37d
    }
}
