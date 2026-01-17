package net.novaware.nes.core.file.ines

import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.System.*
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.*
import static net.novaware.nes.core.util.UnsignedTypes.uint

class ModernHeaderBufferSpec extends Specification {

    def "should pass byte cross checks for Shared_iNES" () {
        given:
        def byte7 = 0
        byte7 |= uint(MAPPER_HI_BITS)
        byte7 |= uint(VERSION_BITS)
        byte7 |= uint(SYSTEM_TYPE_BITS)

        expect:
        byte7 == 0xFF
    }

    def "should put and then get mapper with hi bits" () {
        given:
        def header = headerBuffer()

        when:
        header.putMapper(mapper)
        def actualMapper = header.getMapper()

        then:
        actualMapper == mapper

        where:
        mapper << [0, 1, 2, 15, 16, 129, 255]
    }

    def "should put and then get system type" () {
        given:
        def header = headerBuffer()

        when:
        header.putSystem(system)
        def actualSystem = header.getSystem()

        then:
        actualSystem == system

        where:
        system << [NES, VS_SYSTEM, PLAY_CHOICE_10, EXTENDED]
    }

    def "should put and then get version bits" () {
        given:
        def header = headerBuffer()

        when:
        header.putVersion(version)
        def actualVersion = header.getVersion()

        then:
        actualVersion == version

        where:
        version << [0b00, 0b01, 0b10, 0b11]
    }

    static def headerBuffer() {
        def buffer = UByteBuffer.allocate(NesHeader.SIZE)
        new ModernHeaderBuffer(buffer)
    }
}
