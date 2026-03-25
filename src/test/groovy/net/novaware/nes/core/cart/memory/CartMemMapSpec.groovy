package net.novaware.nes.core.cart.memory

import spock.lang.Specification

import static net.novaware.nes.core.cart.memory.CartMemMap.*

class CartMemMapSpec extends Specification {

    def "should cross check sizes" () {
        expect:
        MEMORY_SIZE == 0xBFE0
        FDS_SIZE == 0x00E0
        RAM_SIZE == 0x2000
        ROM_SIZE == 0x8000

        0xA0E0 == RAM_SIZE +
                FDS_SIZE +
                ROM_SIZE
    }
}
