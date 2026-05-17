package net.novaware.nes.core.config

import spock.lang.Specification

import static net.novaware.nes.core.config.VideoStandard.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class VideoStandardSpec extends Specification {

    def "should calculate frequencies / cycles / refresh rate"() {
        expect:
        standard.getPpuCyclesPerFrame() == ppuCyc
        assertThat(standard.getMasterClock(),       closeTo(masterClk, 0.01d))
        assertThat(standard.getPpuFrequency(),      closeTo(ppuFreq,   0.01d))
        assertThat(standard.getRefreshRate(),       closeTo(refresh,   0.001d))
        assertThat(standard.getCpuFrequency(),      closeTo(cpuFreq,   0.1d))
        assertThat(standard.getCpuCyclesPerFrame(), closeTo(cpuCyc,    0.1d))
        assertThat(standard.getMasterCycles(),      closeTo(masterCyc, 0.1d))

        where:
        standard  | ppuCyc  | ppuFreq       | refresh | cpuCyc    | cpuFreq      | masterCyc | masterClk
        NTSC      |  89_342 | 5_369_318.18d | 60.098d | 29_780.6d | 1_789_772.7d | 357_368d  | 21_477_272.73d
        NTSC_DUAL |  89_342 | 5_369_318.18d | 60.098d | 29_780.6d | 1_789_772.7d | 357_368d  | 21_477_272.73d
        RGB       |  89_342 | 5_369_318.18d | 60.098d | 29_780.6d | 1_789_772.7d | 357_368d  | 21_477_272.73d
        PAL       | 106_392 | 5_320_342.5d  | 50.007d | 33_247.5d | 1_662_607.0d | 531_960d  | 26_601_712.5d
        PAL_DUAL  | 106_392 | 5_320_342.5d  | 50.007d | 33_247.5d | 1_662_607.0d | 531_960d  | 26_601_712.5d
        DENDY     | 106_392 | 5_320_342.5d  | 50.007d | 35_464.0d | 1_773_447.5d | 531_960d  | 26_601_712.5d
        PAL_M     |  89_342 | 5_363_416.5d  | 60.032d | 29_780.6d | 1_787_805.5d | 357_368d  | 21_453_666.0d
    }
}
