package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.NesMeta
import net.novaware.nes.core.util.Quantity
import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.VideoStandard.*
import static net.novaware.nes.core.file.ines.NesHeader.Modern_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Unofficial_iNES.*
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB
import static net.novaware.nes.core.util.QuantityBuilder.banks8kb
import static net.novaware.nes.core.util.UnsignedTypes.*

class NesHeaderSpec extends Specification {

    def "should pass byte cross checks for Modern_iNES" () {
        given:
        def byte9 = 0
        byte9 |= uint(BYTE_9_RESERVED_BITS)
        byte9 |= uint(VIDEO_STANDARD_BITS)

        expect:
        byte9 == 0xFF
    }

    def "should pass byte cross checks for Unofficial_iNES" () {
        given:

        def byte10 = 0
        byte10 |= uint(BYTE_10_RESERVED_BITS)
        byte10 |= uint(BUS_CONFLICTS_BIT)
        byte10 |= uint(PROGRAM_MEMORY_PRESENT_BIT)
        byte10 |= uint(VIDEO_STANDARD_2_BITS)

        expect:
        byte10 == 0xFF
    }

    def "should put and then get program memory size" () {
        given:
        def header = headerBuffer()

        when:
        header.position(BYTE_8)
        putProgramMemory(header, banks8kb(amount).build())
        header.position(BYTE_8)
        Quantity programMemory = getProgramMemory(header)

        then:
        programMemory.amount() == amount
        programMemory.unit() == BANK_8KB

        where:
        amount << [0, 1, 2, 128, 255]
    }

    def "should put and then get video standard" () {
        given:
        def header = headerBuffer()

        when:
        header.position(BYTE_9)
        putVideoStandard(header, videoStandard)
        header.position(BYTE_9)
        NesMeta.VideoStandard actual = getVideoStandard(header)

        then:
        actual == videoStandard

        where:
        videoStandard << [NTSC, PAL] // TODO: what about dual, dendy, etc
    }

    def "should put and then get unofficial byte10" () {
        given:
        def header = headerBuffer()

        Byte10 expected = new Byte10(busConflicts, programMemoryPresent, videoStandard)

        when:
        header.position(BYTE_10)
        putByte10(header, expected)
        header.position(BYTE_10)
        Byte10 actual = getByte10(header)

        then:
        actual == expected

        where:
        busConflicts | programMemoryPresent | videoStandard
        false        | false                | NTSC
        true         | true                 | PAL
        true         | false                | PAL
        false        | true                 | NTSC
        false        | false                | PAL_HYBRID

    }

    static def headerBuffer() {
        UByteBuffer.allocate(NesHeader.SIZE)
    }
}
