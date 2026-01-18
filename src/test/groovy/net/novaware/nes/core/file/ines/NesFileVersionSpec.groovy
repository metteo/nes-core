package net.novaware.nes.core.file.ines

import spock.lang.Specification

import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC_0_7
import static net.novaware.nes.core.file.ines.NesFileVersion.COMPARATOR_NUMERIC
import static net.novaware.nes.core.file.ines.NesFileVersion.FUTURE
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_3
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_5
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_7
import static net.novaware.nes.core.file.ines.NesFileVersion.UNKNOWN

class NesFileVersionSpec extends Specification {

    def "should return correct number of unique versions"() {
        given:
        def valueList = NesFileVersion.valueList()

        when:
        def valueSet = new HashSet(valueList)

        then:
        valueSet.size() == NesFileVersion.values().length - 1 // without the UNKNOWN
        valueSet.size() == valueList.size()
    }

    def "should return values in correct order" () {
        given:
        def valueList = NesFileVersion.valueList()

        when:
        def sortedValues = valueList.stream().sorted(COMPARATOR_NUMERIC).toList()

        then:
        valueList == sortedValues
    }

    def "should return correct history for major versions" () {
        when:
        def history = version.getHistory()

        then:
        history == expectedHistory

        where:
        version     | expectedHistory
        UNKNOWN     | [UNKNOWN]
        MODERN      | [ARCHAIC, ARCHAIC_0_7, MODERN]
        FUTURE      | [ARCHAIC, ARCHAIC_0_7, MODERN, FUTURE]
        MODERN_1_7  | [ARCHAIC, ARCHAIC_0_7, MODERN, MODERN_1_3, MODERN_1_5, MODERN_1_7]
    }
}
