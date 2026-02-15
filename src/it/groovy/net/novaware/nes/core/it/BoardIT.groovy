package net.novaware.nes.core.it

import net.novaware.nes.core.BoardFactory
import spock.lang.Specification

class BoardIT extends Specification {

    def "should create a board"() {
        given:
        BoardFactory factory = BoardFactory.newBoardFactory();

        when:
        def board = factory.newBoard();

        then:
        board != null // no errors
    }
}
