package net.novaware.nes.core.model

import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

class ClockModelsSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        assertThat(ClockModels.NTSC.frequency(), closeTo(21_477_272.73d, 0.01d))
        assertThat(ClockModels.PAL .frequency(), closeTo(26_601_712.50d, 0.01d))
        assertThat(ClockModels.PALM.frequency(), closeTo(21_453_671.33d, 0.01d))
        assertThat(ClockModels.PALN.frequency(), closeTo(21_492_337.50d, 0.01d))
    }
}
