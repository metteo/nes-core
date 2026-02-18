package net.novaware.nes.core.mapper

import spock.lang.Specification

class NROMSpec extends Specification {

    def "should return correct number"() {
        given:
        def mapper = new NROM(1)

        expect:
        mapper.number == 0
    }

    def "should throw on invalid bank count"() {
        when:
        new NROM(banks)

        then:
        thrown(IllegalArgumentException)

        where:
        banks << [0, 3, -1]
    }
}
