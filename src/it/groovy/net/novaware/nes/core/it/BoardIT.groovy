package net.novaware.nes.core.it

import net.novaware.nes.core.BoardFactory
import net.novaware.nes.core.clock.ClockMode
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class BoardIT extends Specification {

    def "should create a board"() {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.1, factor: 2.0)

        BoardFactory factory = BoardFactory.newBoardFactory(ClockMode.LOOP)

        when:
        def board = factory.newBoard()

        board.powerOn()

        then:
        conditions.eventually {
            assert board != null
        }
    }
}
