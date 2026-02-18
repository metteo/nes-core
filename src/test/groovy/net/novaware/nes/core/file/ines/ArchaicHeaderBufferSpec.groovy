package net.novaware.nes.core.file.ines

import net.novaware.nes.core.util.Quantity
import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.Kind.PERSISTENT
import static net.novaware.nes.core.file.NesMeta.Kind.VOLATILE
import static net.novaware.nes.core.file.NesMeta.Layout.*
import static net.novaware.nes.core.file.ines.ArchaicHeaderBuffer.*
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC_0_7
import static net.novaware.nes.core.util.Quantity.Unit.*
import static net.novaware.nes.core.util.UnsignedTypes.sint

class ArchaicHeaderBufferSpec extends Specification {

    UByteBuffer buffer
    ArchaicHeaderBuffer header

    def setup() {
        buffer = NesHeader.allocate()
        header = new ArchaicHeaderBuffer(buffer)
    }

    def "should pass byte cross checks for Archaic iNES" () {
        given:
        def byte6 = 0
        byte6 |= sint(MAPPER_LO_BITS)
        byte6 |= sint(LAYOUT_BITS)
        byte6 |= sint(TRAINER_BIT)
        byte6 |= sint(BATTERY_BIT)

        expect:
        byte6 == 0xFF
    }

    def "should pass byte cross checks for iNES 0.7" () {
        given:
        def byte7 = 0
        byte7 |= sint(MAPPER_HI_BITS)
        byte7 |= sint(BYTE_7_RESERVED_BITS)

        expect:
        byte7 == 0xFF
    }

    def "should put and then get magic" () {
        when:
        header.putMagic()
        def magic = header.getMagic()

        then:
        magic == MAGIC_NUMBER.numbers()

        byte[] bytes = new byte[4];
        buffer.get(BYTE_0, bytes);

        bytes == MAGIC_NUMBER.numbers()

        verifyEach(getRemainingData(BYTE_5).toList()) {it == 0 }
    }

    def "should put and then get program data size" () {
        when:
        header.putProgramData(new Quantity(amount, BANK_16KB))
        Quantity programData = header.getProgramData()

        then:
        programData.amount() == amount
        programData.unit() == BANK_16KB
        // TODO: validate buffer byte/bit
        // TODO: validate other bytes are 0s

        where:
        amount << [0, 1, 2, 128, 255]
    }

    def "should put and then get video data size" () {
        when:
        header.putVideoData(new Quantity(amount, BANK_8KB))
        Quantity videoData = header.getVideoData()

        then:
        videoData.amount() == amount
        videoData.unit() == BANK_8KB

        where:
        amount << [0, 1, 2, 128, 255]
    }

    def "should put and then get byte 6"() {
        given:
        Quantity trainerSize = new Quantity(trainer, BANK_512B)
        def version = ARCHAIC

        when:
        header.putMapper(version, mapper)
                .putVideoMemoryLayout(layout)
                .putTrainer(trainerSize)
                .putProgramMemoryKind(kind)

        def actualMapper = header.getMapper(version)
        def actualLayout = header.getVideoMemoryLayout()
        def actualTrainer = header.getTrainer()
        def actualKind = header.getProgramMemoryKind()

        then:
        actualMapper == mapper
        actualLayout == layout
        actualTrainer == trainerSize
        actualKind == kind

        where:
        mapper | layout                 | trainer | kind
        0      | ALTERNATIVE_VERTICAL   | 1       | PERSISTENT
        3      | STANDARD_HORIZONTAL    | 0       | VOLATILE
        9      | ALTERNATIVE_HORIZONTAL | 1       | VOLATILE
        15     | STANDARD_VERTICAL      | 0       | PERSISTENT
    }

    def "should put and then get mapper with hi bits (iNES 0.7)" () {
        given:
        def version = ARCHAIC_0_7

        when:
        header.putMapper(version, mapper)
        def actualMapper = header.getMapper(version)

        then:
        actualMapper == mapper

        where:
        mapper << [0, 1, 2, 15, 16, 129, 255]
    }

    def "should get byte7 reserved bits" () {
        when:
        header.unwrap().put(BYTE_7, input as byte)
        def byte7 = header.getByte7Reserved()

        then:
        expected == byte7

        where:
        _ | input       | expected
        _ | 0x00        | 0x00
        //| 0brrrr_rrr_ | 0b0rrr_rrrr
        _ | 0b1111_1111 | 0b0000_1111
        _ | 0b1010_1010 | 0b0000_1010
        _ | 0b0101_0101 | 0b0000_0101
    }

    // TODO: test put/get info

    byte[] getRemainingData(int startByte) {
        def maybeZeroes = new byte[NesHeader.SIZE - startByte]
        buffer.get(startByte, maybeZeroes)
        maybeZeroes
    }
}
