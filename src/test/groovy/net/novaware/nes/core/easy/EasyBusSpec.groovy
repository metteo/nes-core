package net.novaware.nes.core.easy

import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.easy.EasyMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte

class EasyBusSpec extends Specification {

    EasyBus bus = new EasyBus()

    def "should write to correct memory segments"() {
        when:
        bus.access(absAddr).write().data(ubyte(data))

        then:
        bus.access(absAddr).read().data() == ubyte(data)
        new TestBus(bus.currentSegment).access(absAddr).read().data() == ubyte(data)

        where:
        absAddr               | data
        RAM_START             | 0x11
        RNG_BYTE              | 0x22
        STACK_SEGMENT_START   | 0x33
        PICTURE_SEGMENT_START | 0x44
        CARTRIDGE_START       | 0x55
        VECTOR_SEGMENT_START  | 0x66
    }
}
