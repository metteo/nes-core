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

    def "should return previously specified values on the bus" () {
        given:
        def openBus = new OpenBus(ushort(0x6000), ushort(0x7FFF))
        def address = ushort(0x6789)

        // prime the bus with "old" values
        openBus.onAccess(address)
        openBus.onWrite(ubyte(0x67))

        expect:
        openBus.lastAccess() == address
        openBus.onRead() == ubyte(0x67)
    }
}
