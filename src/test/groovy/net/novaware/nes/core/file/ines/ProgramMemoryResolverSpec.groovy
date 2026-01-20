package net.novaware.nes.core.file.ines

import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.Kind.*
import static net.novaware.nes.core.file.NesMeta.ProgramMemory
import static net.novaware.nes.core.file.ProgramMemoryBuilder.*
import static net.novaware.nes.core.util.QuantityBuilder.banks8kb

class ProgramMemoryResolverSpec extends Specification {

    ProgramMemoryResolver resolver

    def setup() {
        resolver = new ProgramMemoryResolver()
    }

    def "should resolve different combinations of program memory params" () {
        given:

        when:
        ProgramMemory actual = resolver.resolve(kind, size.build(), presence)

        then:
        actual == expected.build()

        where:
        kind       | size           | presence || expected
        // battery bit set
        //     presence bit unset
        PERSISTENT | banks8kb(0)    | UNKNOWN  || battery8kb(1) // accept default
        PERSISTENT | banks8kb(1)    | UNKNOWN  || battery8kb(1)
        PERSISTENT | banks8kb(2)    | UNKNOWN  || battery8kb(2)
        PERSISTENT | banks8kb(4)    | UNKNOWN  || battery8kb(4)

        // battery bit set
        //     presence bit set (probably garbage)
        PERSISTENT | banks8kb(0)    | NONE     || battery8kb(1) // accept default
        PERSISTENT | banks8kb(1)    | NONE     || battery8kb(1)
        PERSISTENT | banks8kb(2)    | NONE     || battery8kb(2)
        PERSISTENT | banks8kb(4)    | NONE     || battery8kb(4)

        // battery bit unset
        //     presence bit unset
        VOLATILE   | banks8kb(0)    | UNKNOWN  || volatile8kb(1) // accept default
        VOLATILE   | banks8kb(1)    | UNKNOWN  || volatile8kb(1)
        VOLATILE   | banks8kb(2)    | UNKNOWN  || volatile8kb(2)
        VOLATILE   | banks8kb(4)    | UNKNOWN  || volatile8kb(4)

        //     presence bit set (force open bus if size not specified)
        VOLATILE   | banks8kb(0)    | NONE     || none()         // prevent default
        VOLATILE   | banks8kb(1)    | NONE     || volatile8kb(1) // accept specific sizes
        VOLATILE   | banks8kb(2)    | NONE     || volatile8kb(2)
        VOLATILE   | banks8kb(4)    | NONE     || volatile8kb(4)
    }

    // TODO: create tests for validation
}
