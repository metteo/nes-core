package net.novaware.nes.core.tv

import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class ColorNtscSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        assertThat(ColorNtsc.I_FRAME_RATE, closeTo(       29.97d, 0.01d))
        assertThat(ColorNtsc.FIELD_RATE,   closeTo(       59.94d, 0.01d))
        assertThat(ColorNtsc.P_FRAME_RATE, closeTo(       59.94d, 0.01d))

        assertThat(ColorNtsc.I_LINE_RATE,  closeTo(   15_734.27d, 0.01d))

        assertThat(ColorNtsc.SUBCARRIER,   closeTo(3_579_545.45d, 0.01d))
    }
}
