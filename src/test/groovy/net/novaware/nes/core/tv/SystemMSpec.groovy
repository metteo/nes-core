package net.novaware.nes.core.tv

import spock.lang.Specification

class SystemMSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        SystemM.ACTIVE_I_LINES == 480
        SystemM.I_LINE_RATE    == 15750

        SystemM.TOTAL_P_LINES  == 262
        SystemM.BLANK_P_LINES  == 22
        SystemM.ACTIVE_P_LINES == 240

        SystemM.FIELD_RATE     == 60
    }
}
