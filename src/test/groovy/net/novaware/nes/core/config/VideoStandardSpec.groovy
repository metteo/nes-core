package net.novaware.nes.core.config

import spock.lang.Specification

import static net.novaware.nes.core.config.VideoStandard.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class VideoStandardSpec extends Specification {

    def "should calculate refresh rate correctly"() { // TODO: convert to data table
        given:
        def delta = 0.001d
        expect:
        assertThat(NTSC.getRefreshRate(),      closeTo(60.098d, delta))
        assertThat(NTSC_DUAL.getRefreshRate(), closeTo(60.098d, delta))
        assertThat(PAL.getRefreshRate(),       closeTo(50.007d, delta))
        assertThat(PAL_DUAL.getRefreshRate(),  closeTo(50.007d, delta))
        assertThat(DENDY.getRefreshRate(),     closeTo(50.007d, delta))
        assertThat(PAL_M.getRefreshRate(),     closeTo(60.032d, delta))
    }

    // TODO: verify cpu / ppu frequencies
}
