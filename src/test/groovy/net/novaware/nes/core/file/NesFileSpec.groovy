package net.novaware.nes.core.file

import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.Layout.*

class NesFileSpec extends Specification {

    def "should properly map layout to bits"() {
        expect:
        STANDARD_VERTICAL.bits()      == 0b0000
        STANDARD_HORIZONTAL.bits()    == 0b0001
        ALTERNATIVE_VERTICAL.bits()   == 0b1000
        ALTERNATIVE_HORIZONTAL.bits() == 0b1001
    }
}
