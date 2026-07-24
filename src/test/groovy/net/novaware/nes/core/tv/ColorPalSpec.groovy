package net.novaware.nes.core.tv

import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class ColorPalSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        ColorPal.I_FRAME_RATE == 25d
        ColorPal.FIELD_RATE   == 50d
        ColorPal.P_FRAME_RATE == 50d

        ColorPal.I_LINE_RATE  == 15625d

        assertThat(ColorPal.SUBCARRIER, closeTo(4_433_618.75d, 0.01d))
    }
}
