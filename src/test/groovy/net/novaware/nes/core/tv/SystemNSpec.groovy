package net.novaware.nes.core.tv

import spock.lang.Specification

class SystemNSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        SystemN.ACTIVE_I_LINES == 576
        SystemN.I_LINE_RATE    == 15625

        SystemN.TOTAL_P_LINES  == 312
        SystemN.BLANK_P_LINES  == 24
        SystemN.ACTIVE_P_LINES == 288
        SystemN.P_FRAME_RATE   == 50
    }
}
