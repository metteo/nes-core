package net.novaware.nes.core.file.ines

import net.novaware.nes.core.util.Quantity
import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import static net.novaware.nes.core.file.NesMeta.Kind.PERSISTENT
import static net.novaware.nes.core.file.NesMeta.Kind.VOLATILE
import static net.novaware.nes.core.file.NesMeta.Layout.*
import static net.novaware.nes.core.file.ines.ArchaicHeaderBuffer.*
import static net.novaware.nes.core.util.Quantity.Unit.*
import static net.novaware.nes.core.util.UnsignedTypes.uint

class ArchaicHeaderBufferSpec extends Specification {


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

    def "should put and then get program data size" () {
        given:
        def header = headerBuffer()

        when:
        header.putProgramData(new Quantity(amount, BANK_16KB))
        // TODO: maybe get binary data and validate?
        Quantity programData = header.getProgramData()

        then:
        programData.amount() == amount
        programData.unit() == BANK_16KB

        where:
        amount << [0, 1, 2, 128, 255]
    }

    def "should put and then get video data size" () {
        given:
        def header = headerBuffer()

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
        def header = headerBuffer()

        Quantity trainerSize = new Quantity(trainer, BANK_512B)

        when:
        header.putMapper(mapper)
                .putMemoryLayout(layout)
                .putTrainer(trainerSize)
                .putMemoryKind(kind)

        def actualMapper = header.getMapper()
        def actualLayout = header.getMemoryLayout()
        def actualTrainer = header.getTrainer()
        def actualKind = header.getMemoryKind()

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

    // TODO: test put/get info

    static def headerBuffer() {
        def buffer = UByteBuffer.allocate(NesHeader.SIZE)
        new ArchaicHeaderBuffer(buffer)
    }
}
