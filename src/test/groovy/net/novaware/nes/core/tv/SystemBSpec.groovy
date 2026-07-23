package net.novaware.nes.core.tv

import spock.lang.Specification

class SystemBSpec extends Specification {

    def "should calculate constants correctly"() {
        expect:
        SystemB.ACTIVE_I_LINES == 576
        SystemB.I_LINE_RATE    == 15625

        SystemB.TOTAL_P_LINES  == 312
        SystemB.BLANK_P_LINES  == 24
        SystemB.ACTIVE_P_LINES == 288

        SystemB.FIELD_RATE     == 50
    }
}
