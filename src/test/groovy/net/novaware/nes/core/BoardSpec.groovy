package net.novaware.nes.core

import net.novaware.nes.core.clock.LoopedClockGenerator
import net.novaware.nes.core.cpu.unit.ClockGenerator
import spock.lang.Specification

class BoardSpec extends Specification { // TODO: rename to BoardCT since we don't use mocks here

    def "should instantiate"() {
        given:
        ClockGenerator clock = new LoopedClockGenerator()
        BoardFactory factory = BoardFactory.newBoardFactory(clock);

        when:
        def board = factory.newBoard();

        then:
        board != null // no errors
    }
}
