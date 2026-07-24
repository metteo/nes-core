package net.novaware.nes.core.tv

import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class ColorPalNSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        ColorPalN.I_FRAME_RATE == 25d
        ColorPalN.FIELD_RATE   == 50d
        ColorPalN.P_FRAME_RATE == 50d

        ColorPalN.I_LINE_RATE  == 15625d

        assertThat(ColorPalN.SUBCARRIER, closeTo(3_582_056.25d, 0.01d))
    }
}
