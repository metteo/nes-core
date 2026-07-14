package net.novaware.nes.core.util

import spock.lang.Specification

import java.nio.ByteBuffer

import static net.novaware.nes.core.util.UTypes.ubyte

class UByteBufferSpec extends Specification {

    def "should allocate an instance"() {
        when:
        def instance = UByteBuffer.allocate(1)

        then:
        instance.toString() == "UByteBuffer[cap=1]"
        instance.capacity() == 1
    }

    def "should throw on negative capacity"() {
        when:
        UByteBuffer.allocate(-1)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "capacity must be non-negative"
    }

    def "should create out of given ByteBuffer"() {
        given:
        def input = 0xAB
        def original = ByteBuffer.allocate(1)
        original.put((byte) input)

        when:
        def ucopy = UByteBuffer.of(original)

        then:
        ucopy.capacity() == 1
        ucopy.getAsInt(0) == input
        ucopy.get(0) == ubyte(input)
    }

    def "should allocate empty instance"() {
        when:
        def empty = UByteBuffer.empty()

        then:
        empty.capacity() == 0
    }

    def "should put and then get data at the ends"() {
        given:
        def buffer = UByteBuffer.allocate(capacity)

        when:
        buffer.put(0, ubyte(0xAB))
        buffer.putAsByte(capacity - 1, 0xCD)

        then:
        buffer.get(0) == ubyte(0xAB)
        buffer.getAsInt(capacity - 1) == 0xCD

        where:
        capacity << [2, 4, 256]
    }

    def "should fill the buffer with a constant"() {
        given:
        def val = 0xAB
        def buffer = UByteBuffer.allocate(2)
            .fill(ubyte(val))

        when:
        def first = buffer.getAsInt(0)
        def last = buffer.getAsInt(1)

        then:
        first == val
        last == val
    }

    def "should fill the buffer with supplied values"() {
        given:
        def values = [0xAB, 0xCD].iterator()
        def supplier = { -> ubyte(values.next())}

        def buffer = UByteBuffer.allocate(2)
                .fill(supplier)

        when:
        def first = buffer.getAsInt(0)
        def last = buffer.getAsInt(1)

        then:
        first == 0xAB
        last == 0xCD
    }

    def "should fill the buffer with supplied array"() {
        given:
        def values = (byte[])[0xAB, 0xCD]

        def buffer = UByteBuffer.allocate(2)
                .fill(0, values)

        when:
        def first = buffer.getAsInt(0)
        def last = buffer.getAsInt(1)

        then:
        first == 0xAB
        last == 0xCD
    }
}
