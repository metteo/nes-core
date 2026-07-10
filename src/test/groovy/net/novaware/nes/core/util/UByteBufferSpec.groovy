package net.novaware.nes.core.util

import spock.lang.Specification

class UByteBufferSpec extends Specification {

    def "should allocate an instance"() {
        when:
        def instance = UByteBuffer.allocate(1)

        then:
        instance.toString() == "UByteBuffer[1]"
        instance.capacity() == 1
    }

}
