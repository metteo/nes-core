package net.novaware.nes.core.file

import spock.lang.Specification

class NesFileSpec extends Specification {

    def "should properly map layout to bits"() {
        expect:
        NesMeta.Layout.STANDARD_VERTICAL.bits() == 0b0000
        NesMeta.Layout.STANDARD_HORIZONTAL.bits() == 0b0001
        NesMeta.Layout.ALTERNATIVE_VERTICAL.bits() == 0b1000
        NesMeta.Layout.ALTERNATIVE_HORIZONTAL.bits() == 0b1001
    }
}
