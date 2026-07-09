package net.novaware.nes.core.util

import spock.lang.Specification

class UByteBufferSpec extends Specification {

    def "should allocate an instance"() {
        when:
        def instance = UByteBuffer.allocate(1)

        then:
        instance.toString() == "java.nio.HeapByteBuffer[pos=0 lim=1 cap=1]"
        instance.capacity() == 1
    }

}
