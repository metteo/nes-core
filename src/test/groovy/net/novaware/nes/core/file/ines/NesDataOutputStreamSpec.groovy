package net.novaware.nes.core.file.ines

import net.novaware.nes.core.util.Hex
import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import java.nio.ByteBuffer

import static net.novaware.nes.core.file.NesDataBuilder.emptyData
import static net.novaware.nes.core.file.NesDataBuilder.nesData
import static net.novaware.nes.core.file.NesDataBuilder.watermarkedData
import static net.novaware.nes.core.file.ines.NesDataOutputStream.DEFAULT_TRANSFER_BUFFER_SIZE

class NesDataOutputStreamSpec extends Specification {

    def "should write empty nes data"() {
        given:
        def data = emptyData().build()

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        NesDataOutputStream nesFileOutputStream = new NesDataOutputStream(out)

        when:
        nesFileOutputStream.write(data)

        then:
        out.size() == 0
    }

    def "should write single byte sized nes data" () {
        given:
        def data = nesData()
                .header(UByteBuffer.of(singleByteBuffer(0x01)))
                .trainer(singleByteBuffer(0x2))
                .program(singleByteBuffer(0x4))
                .video(singleByteBuffer(0x8))
                .misc(singleByteBuffer(0x10))
                .footer(singleByteBuffer(0x20))
                .build()

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        NesDataOutputStream nesFileOutputStream = new NesDataOutputStream(out, transferBuffer)

        when:
        nesFileOutputStream.write(data)

        then:
        out.size() == 6
        Hex.s(out.toByteArray()) == "01 02 04 08 10 20"

        where:
        transferBuffer << [1, 2, DEFAULT_TRANSFER_BUFFER_SIZE]
    }

    def "should write water marked nes data" () {
        given:
        def data = watermarkedData().build()

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        NesDataOutputStream nesFileOutputStream = new NesDataOutputStream(out, transferBuffer)

        when:
        nesFileOutputStream.write(data)

        then:
        out.size() == data.size()

        where:
        transferBuffer << [1, 2, DEFAULT_TRANSFER_BUFFER_SIZE]
    }

    def singleByteBuffer(int content) {
        ByteBuffer.allocate(1)
                .put((byte) content)
                .flip()
    }
}
