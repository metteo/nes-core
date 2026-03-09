package net.novaware.nes.core.file.ines

import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.System.*
import static net.novaware.nes.core.file.NesMeta.VideoStandard.*
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.*
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB
import static net.novaware.nes.core.util.QuantityBuilder.banks8kb
import static net.novaware.nes.core.util.UTypes.sint

class ModernHeaderBufferSpec extends Specification {

    def "should pass byte cross checks for byte7" () {
        given:
        def byte7 = 0
        byte7 |= sint(MAPPER_HI_BITS)
        byte7 |= sint(VERSION_BITS)
        byte7 |= sint(SYSTEM_TYPE_BITS)

        expect:
        byte7 == 0xFF
    }

    def "should pass byte cross checks for byte9" () {
        given:
        def byte9 = 0
        byte9 |= sint(BYTE_9_RESERVED_BITS)
        byte9 |= sint(VIDEO_STANDARD_BITS)

        expect:
        byte9 == 0xFF
    }

    def "should pass byte cross checks for byte10 (unofficial)" () {
        given:

        def byte10 = 0
        byte10 |= sint(BYTE_10_RESERVED_BITS)
        byte10 |= sint(BUS_CONFLICTS_BIT)
        byte10 |= sint(PROGRAM_MEMORY_ABSENT_BIT)
        byte10 |= sint(VIDEO_STANDARD_EXT_BITS)

        expect:
        byte10 == 0xFF
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

    def "should put and then get program memory size" () {
        given:
        def header = headerBuffer()

        when:
        header.putProgramMemory(banks8kb(amount).build())
        def programMemory = header.getProgramMemory()

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
        header.putVideoStandard(input)
        def actual = header.getVideoStandard()

        then:
        actual == expected

        where:
        input     | expected // downgrade dual to single (there is no space)
        NTSC      | NTSC
        NTSC_DUAL | NTSC
        PAL       | PAL
        PAL_DUAL  | PAL
    }

    def "should get byte9 reserved bits" () {
        given:
        def header = headerBuffer()

        when:
        header.unwrap().put(BYTE_9, input as byte)
        def byte9 = header.getByte9Reserved()

        then:
        expected == byte9

        where:
        _ | input       | expected
        _ | 0x00        | 0x00
        //| 0brrrr_rrr_ | 0b0rrr_rrrr
        _ | 0b1111_1111 | 0b0111_1111
        _ | 0b1010_1010 | 0b0101_0101
        _ | 0b0101_0101 | 0b0010_1010
    }

    def "should put and then get bus conflicts" () {
        given:
        def header = headerBuffer()

        when:
        header.putBusConflicts(busConflicts) // TODO: maybe check the data in buffer too
        def actual = header.getBusConflicts()

        then:
        actual == busConflicts

        where:
        busConflicts << [true, false]
    }

    def "should put and then get program memory absent bit" () {
        given:
        def header = headerBuffer()

        when:
        header.putProgramMemoryAbsent(absent)
        def actual = header.isProgramMemoryAbsent()

        then:
        actual == absent

        where:
        absent << [true, false]
    }

    def "should put and then get unofficial video standard extended in byte10" () {
        given:
        def header = headerBuffer()

        when:
        header.putVideoStandardExt(videoStandardExt)
        def actual = header.getVideoStandardExt()

        then:
        actual == videoStandardExt

        where:
        videoStandardExt << [NTSC, NTSC_DUAL, PAL, PAL_DUAL]
    }

    def "should get byte10 reserved bits" () {
        given:
        def header = headerBuffer()

        when:
        header.unwrap().put(BYTE_10, input as byte)
        def byte10 = header.getByte10Reserved()

        then:
        expected == byte10

        where:
        _ | input       | expected
        _ | 0x00        | 0x00
        //| 0brr___rr__ | 0brr___rr__
        _ | 0b1111_1111 | 0b1100_1100
        _ | 0b1010_1010 | 0b1000_1000
        _ | 0b0101_0101 | 0b0100_0100
    }

    static def headerBuffer() {
        def buffer = UByteBuffer.allocate(NesHeader.SIZE)
        new ModernHeaderBuffer(buffer)
    }
}
