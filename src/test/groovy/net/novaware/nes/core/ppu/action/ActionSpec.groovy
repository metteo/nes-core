package net.novaware.nes.core.ppu.action

import spock.lang.Specification

class ActionSpec extends Specification {

    def "should have correct number of actions"() {
        expect:
        Action.values().length == 31
    }
}
