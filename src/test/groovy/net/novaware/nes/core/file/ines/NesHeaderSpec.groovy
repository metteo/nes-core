package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.NesMeta
import net.novaware.nes.core.util.Quantity
import spock.lang.Specification

import java.nio.ByteBuffer

import static net.novaware.nes.core.file.NesMeta.Kind.PERSISTENT
import static net.novaware.nes.core.file.NesMeta.Kind.VOLATILE
import static net.novaware.nes.core.file.NesMeta.Layout.*
import static net.novaware.nes.core.file.NesMeta.System.*
import static net.novaware.nes.core.file.NesMeta.VideoStandard.*
import static net.novaware.nes.core.file.ines.NesHeader.Archaic_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Modern_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Shared_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Unofficial_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Version.MODERN_iNES
import static net.novaware.nes.core.file.ines.NesHeader.Version.NES_2_0
import static net.novaware.nes.core.util.Quantity.Unit.*
import static net.novaware.nes.core.util.QuantityBuilder.banks8kb
import static net.novaware.nes.core.util.UnsignedTypes.*

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

    def "should put and then get program data size" () {
        given:
        ByteBuffer header = headerBuffer()

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
    
    def "should put and then get byte 7" () {
        given:
        ByteBuffer header = headerBuffer()

        when:
        header.position(BYTE_7)
        putByte7(header, system, (short) mapper, version)
        header.position(BYTE_7)
        Byte7 actual = getByte7(header)

        then:
        ubyte(actual.systemTypeBits()) == system.bits()
        ushort(actual.mapperHi()) == ushort(mapper & 0xF0)
        ubyte(actual.versionBits()) == (byte) (version == NES_2_0 ? 0b10 : 0)

        where:
        system         | mapper | version
        NES            | 0      | MODERN_iNES
        VS_SYSTEM      | 16     | MODERN_iNES
        PLAY_CHOICE_10 | 129    | MODERN_iNES
        EXTENDED       | 255    | MODERN_iNES
    }

    def "should put and then get program memory size" () {
        given:
        ByteBuffer header = headerBuffer()

        when:
        header.position(BYTE_8)
        putProgramMemory(header, banks8kb(inAmount).build())
        header.position(BYTE_8)
        Quantity programMemory = getProgramMemory(header)

        then:
        programMemory.amount() == outAmount
        programMemory.unit() == BANK_8KB

        where:     // v- default to 8KB if 0
        inAmount  << [0, 1, 2, 128, 255]
        outAmount << [1, 1, 2, 128, 255]
    }

    def "should put and then get video standard" () {
        given:
        ByteBuffer header = headerBuffer()

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
        ByteBuffer header = headerBuffer()

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
        ByteBuffer.allocate(NesHeader.SIZE)
    }
}
