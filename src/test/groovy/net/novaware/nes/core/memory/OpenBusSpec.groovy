package net.novaware.nes.core.memory

import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class OpenBusSpec extends Specification {

    def "should hold start and end address"() {
        given:
        def openBus = new OpenBus(ushort(0x1000), ushort(0x1FFF))

        expect:
        openBus.getStartAddress() == ushort(0x1000)
        openBus.getEndAddress() == ushort(0x1FFF)
    }

    def "should return high byte of address" () {
        given:
        def openBus = new OpenBus(ushort(0x6000), ushort(0x7FFF))

        expect:
        openBus.specifyThen(ushort(0x6789)).readByte() == ubyte(0x67)
    }
}
