package net.novaware.nes.core.memory


import spock.lang.Specification

import static net.novaware.nes.core.memory.RecordingBus.*
import static net.novaware.nes.core.memory.RecordingBus.OpType.ACCESS
import static net.novaware.nes.core.memory.RecordingBus.OpType.READ
import static net.novaware.nes.core.memory.RecordingBus.OpType.WRITE
import static net.novaware.nes.core.util.UnsignedTypes.*

class RecordingBusSpec extends Specification {

    RecordingBus bus = new RecordingBus()

    def "should increase cycle counter when calling specify"() {
        given:
        bus.record()

        when:
        bus.specify(ushort(0xFFFF))

        then:
        bus.cycles() == 1
    }

    def "should increase cycle counter when calling specifyAnd"() {
        given:
        bus.record()

        when:
        bus.specifyAnd(ushort(0x0000))

        then:
        bus.cycles() == 1
    }

    def "should read 0 from any address when empty"() {
        given:
        bus.record()

        when:
        def b = bus.readByte()

        then:
        b == UBYTE_0
        bus.cycles() == 0
    }

    def "should write to any address"() {
        given:
        bus.record()
        def b = ubyte(0x55)

        when:
        bus.writeByte(b)

        then:
        bus.readByte() == b
    }

    def "should write and then read from specified address"() {
        given:
        bus.record()
        def b = ubyte(0xAA)
        def c = ubyte(0xDD)

        when:
        bus.specifyAnd(ushort(0xBBBB)).writeByte(b)
        bus.specifyAnd(ushort(0xCCCC)).writeByte(c)

        then:
        bus.readByte() == c
        bus.cycles() == 2
        bus.address() == ushort(0xCCCC) // last address
        bus.data() == c // last data
    }

    def "should record access, read and write operations"() {
        given:
        bus.record()

        when:
        bus.specify(ushort(0x1111))
        bus.specify(ushort(0x2222))
        bus.readByte()
        bus.specify(ushort(0x3333))
        bus.writeByte(ubyte(0x44))
        bus.specify(ushort(0x5555))
        bus.specifyAnd(ushort(0x3333)).readByte()

        then:
        bus.cycles() == 5
        bus.activity() == [
            new Op(ACCESS, 0x1111, 0),
            new Op(ACCESS, 0x2222, 0),
            new Op(READ,   0x2222, 0),
            new Op(ACCESS, 0x3333, 0),
            new Op(WRITE,  0x3333, 0x44),
            new Op(ACCESS, 0x5555, 0),
            new Op(ACCESS, 0x3333, 0),
            new Op(READ,   0x3333, 0x44),
        ]
    }
}
