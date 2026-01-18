package net.novaware.nes.core.file.ines

import spock.lang.Specification

class NesFileVersionSpec extends Specification {

    def "should return correct number of unique versions"() {
        given:
        def values = NesFileVersion.values()

        when:
        def valueSet = new HashSet(values)

        then:
        7 == valueSet.size() // without the UNKNOWN
        valueSet.size() == values.size()
    }

    def "should return values in correct order" () {
        given:
        def values = NesFileVersion.values()

        when:
        def sortedValues = values.stream().sorted().toList()

        then:
        values == sortedValues
    }
}
