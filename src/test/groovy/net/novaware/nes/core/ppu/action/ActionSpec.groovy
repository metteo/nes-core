package net.novaware.nes.core.ppu.action

import spock.lang.Specification

class ActionSpec extends Specification {

    def "should have correct number of actions total / per category"() {
        expect:
        Action.values().length == 32
        countByCategory(ActionCategory.BUS) == 17
        countByCategory(ActionCategory.OAM) == 2
        countByCategory(ActionCategory.DRAW) == 2
        countByCategory(ActionCategory.VIEW) == 4
        countByCategory(ActionCategory.FLAG) == 4
        countByCategory(ActionCategory.MISC) == 2
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
