package net.novaware.nes.core.config

import spock.lang.Specification

class BorderRegionTest extends Specification {

    def "should return border region by video standard"() {
        expect:
        borderRegion == BorderRegion.of(videoStandard)

        where:
        videoStandard      | borderRegion
        VideoStandard.NTSC | BorderRegion.NTSC
        VideoStandard.PAL  | BorderRegion.PAL
    }
}
