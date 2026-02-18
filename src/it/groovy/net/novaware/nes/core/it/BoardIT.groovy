package net.novaware.nes.core.it

import net.novaware.nes.core.BoardFactory
import net.novaware.nes.core.clock.LoopedClockGenerator
import net.novaware.nes.core.cpu.unit.ClockGenerator
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class BoardIT extends Specification {

    def "should create a board"() {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.1, factor: 2.0)

        ClockGenerator clock = new LoopedClockGenerator()
        BoardFactory factory = BoardFactory.newBoardFactory(clock)

        when:
        def board = factory.newBoard()

        board.powerOn()

        then:
        conditions.eventually {
            assert board != null
        }
    }
}
