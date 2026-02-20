package net.novaware.nes.core.memory

import net.novaware.nes.core.easy.EasyBus
import spock.lang.Specification

import static net.novaware.nes.core.easy.EasyMap.*
import static net.novaware.nes.core.util.UTypes.ubyte

class EasyBusSpec extends Specification {

    EasyBus bus = new EasyBus()

    def "should write to correct memory segments"() {
        when:
        bus.specifyThen(absAddr).writeByte(ubyte(data))

        then:
        bus.readByte() == ubyte(data)
        bus.currentSegment.specifyThen(absAddr).readByte() == ubyte(data)

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
