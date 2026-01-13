package net.novaware.nes.core.file.ines


import net.novaware.nes.core.util.Quantity
import spock.lang.Specification

import java.nio.ByteBuffer

import static net.novaware.nes.core.file.NesMeta.Kind.PERSISTENT
import static net.novaware.nes.core.file.NesMeta.Kind.VOLATILE
import static net.novaware.nes.core.file.NesMeta.Layout.*
import static net.novaware.nes.core.file.ines.NesHeader.Archaic_iNES.*

import static net.novaware.nes.core.file.ines.NesHeader.Modern_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Unofficial_iNES.*
import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB
import static net.novaware.nes.core.util.Quantity.Unit.BANK_512B
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB
import static net.novaware.nes.core.util.UnsignedTypes.uint

class NesHeaderSpec extends Specification {

    def "should pass byte cross checks for Archaic_iNES" () {
        given:
        def byte6 = 0
        byte6 |= uint(MAPPER_LO_BITS)
        byte6 |= uint(LAYOUT_BITS)
        byte6 |= uint(TRAINER_BIT)
        byte6 |= uint(BATTERY_BIT)

        expect:
        byte6 == 0xFF
    }

    def "should pass byte cross checks for Shared_iNES" () {
        given:
        def byte7 = 0
        byte7 |= uint(MAPPER_HI_BITS)
        byte7 |= uint(VERSION_BITS)
        byte7 |= uint(SYSTEM_TYPE_BITS)

        expect:
        byte7 == 0xFF
    }

    def "should pass byte cross checks for Modern_iNES" () {
        given:
        def byte9 = 0
        byte9 |= uint(BYTE_9_RESERVED_BITS)
        byte9 |= uint(VIDEO_STANDARD_BITS)

        def byte10 = 0
        byte10 |= uint(BYTE_10_RESERVED_BITS)
        byte10 |= uint(PROGRAM_MEMORY_PRESENT_BIT)
        byte10 |= uint(VIDEO_STANDARD_2_BITS)

        expect:
        byte9 == 0xFF
        byte10 == 0xFF
    }

    def "should pass byte cross checks for Unofficial_iNES" () {
        given:

        def byte10 = 0
        byte10 |= uint(BYTE_10_RESERVED_BITS)
        byte10 |= uint(PROGRAM_MEMORY_PRESENT_BIT)
        byte10 |= uint(VIDEO_STANDARD_2_BITS)

        expect:
        byte10 == 0xFF
    }

    def "should put and then get program data size" () {
        given:
        ByteBuffer header = ByteBuffer.allocate(NesHeader.SIZE)

        when:
        header.position(BYTE_4)
        putProgramData(header, new Quantity(amount, BANK_16KB))
        header.position(BYTE_4)
        Quantity programData = getProgramData(header)

        then:
        programData.amount() == amount
        programData.unit() == BANK_16KB

        where:
        amount << [0, 1, 2, 128, 255]
    }

    def "should put and then get video data size" () {
        given:
        ByteBuffer header = headerBuffer()

        when:
        header.position(BYTE_5)
        putVideoData(header, new Quantity(amount, BANK_8KB))
        header.position(BYTE_5)
        Quantity videoData = getVideoData(header)

        then:
        videoData.amount() == amount
        videoData.unit() == BANK_8KB

        where:
        amount << [0, 1, 2, 128, 255]
    }

    def "should put and then get byte 6"() {
        given:
        ByteBuffer header = headerBuffer()

        Byte6 expected = new Byte6(
                (short) mapper,
                layout,
                new Quantity(trainer, BANK_512B),
                kind
        )

        when:
        header.position(BYTE_6)
        putByte6(header, expected)
        header.position(BYTE_6)
        Byte6 actual = getByte6(header)

        then:
        expected == actual

        where:
        mapper | layout                 | trainer | kind
        0      | ALTERNATIVE_VERTICAL   | 1       | PERSISTENT
        3      | STANDARD_HORIZONTAL    | 0       | VOLATILE
        9      | ALTERNATIVE_HORIZONTAL | 1       | VOLATILE
        15     | STANDARD_VERTICAL      | 0       | PERSISTENT
    }

    static def headerBuffer() {
        ByteBuffer.allocate(NesHeader.SIZE)
    }
}
