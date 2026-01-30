package net.novaware.nes.core;

import spock.lang.Specification;

class BoardSpec extends Specification {

    def "should instantiate"() {
        given:
        BoardFactory factory = BoardFactory.newBoardFactory();

        when:
        def board = factory.newBoard();

        then:
        board != null // no errors
    }
}
