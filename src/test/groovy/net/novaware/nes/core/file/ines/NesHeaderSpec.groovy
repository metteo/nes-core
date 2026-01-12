package net.novaware.nes.core.file.ines

import spock.lang.Specification

import static net.novaware.nes.core.file.ines.NesHeader.Archaic_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Modern_iNES.*
import static net.novaware.nes.core.file.ines.NesHeader.Unofficial_iNES.*
import static net.novaware.nes.core.util.UnsignedTypes.uint

class NesHeaderSpec extends Specification {

    def "should pass byte cross checks for Archaic_iNES" () {
        given:
        def byte6 = 0
        byte6 |= uint(MAPPER_LO_BITS)
        byte6 |= uint(MIRRORING_BITS)
        byte6 |= uint(TRAINER_BIT)
        byte6 |= uint(BATTERY_BIT)

        expect:
        byte6 == 0xFF
    }

    def "should pass byte cross checks for Shared_iNES" () {
        given:
        def byte7 = 0
        byte7 |= uint(MAPPER_HI_BITS)
        byte7 |= uint(NES_2_0_BITS)
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
}
