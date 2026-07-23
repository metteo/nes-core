package net.novaware.nes.core.tv

import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class ColorPalMSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        assertThat(ColorPalM.SUBCARRIER , closeTo(3_575_611.89d, 0.01d))
        assertThat(ColorPalM.I_LINE_RATE, closeTo(   15_734.26d, 0.01d))
        assertThat(ColorPalM.FRAME_RATE , closeTo(       29.97d, 0.01d))
        assertThat(ColorPalM.FIELD_RATE , closeTo(       59.94d, 0.01d))
    }
}
