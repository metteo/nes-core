package net.novaware.nes.core.ppu.action

import spock.lang.Specification

class ActionSpec extends Specification {

    def "should have correct number of actions total / per category"() {
        expect:
        Action.values().length == 29
        countByCategory(ActionCategory.BUS) == 14 // 4 bits
        countByCategory(ActionCategory.OAM) == 2  // 2 bits
        countByCategory(ActionCategory.DRAW) == 2 // 2 bits
        countByCategory(ActionCategory.VIEW) == 4 // 3 bits
        countByCategory(ActionCategory.FLAG) == 4 // 3 bits
        countByCategory(ActionCategory.MISC) == 2 // 1 bit shift, NOOP 0s
        countByCategory(ActionCategory.UNKNOWN) == 1
    }

    static long countByCategory(ActionCategory category) {
        Action.stream().filter({ it.getCategory() == category }).count()
    }

    def "should have unique mnemonics"() {
        expect:
        Action.stream()
                .map(Action::getMnemonic)
                .distinct()
                .count() == Action.values().length
    }
}
