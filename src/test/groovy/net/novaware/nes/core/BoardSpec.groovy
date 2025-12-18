package net.novaware.nes.core;

import spock.lang.Specification;

class BoardSpec extends Specification {

    def "should instantiate"() {
        expect:
        new Board() != null // no errors
    }
}
