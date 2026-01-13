package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.NesMetaBuilder
import net.novaware.nes.core.file.NesMeta
import spock.lang.Specification

import java.nio.ByteBuffer

import static java.nio.charset.StandardCharsets.US_ASCII
import static net.novaware.nes.core.file.ines.NesHeader.Shared_iNES.BYTE_7
import static net.novaware.nes.core.file.ines.NesHeader.Version.ARCHAIC_iNES
import static net.novaware.nes.core.file.ines.NesHeader.Version.MODERN_iNES
import static net.novaware.nes.core.file.ines.NesHeader.Version.NES_2_0
import static net.novaware.nes.core.util.UnsignedTypes.ubyte

class NesHeaderReaderSpec extends Specification {

    // region detectVersion

    def "should detect archaic header with D at byte 7" () {
        given:
        def headerBuffer = baseMarioBros()
                .put(BYTE_7, asciiBytes("D"))
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == ARCHAIC_iNES
    }

    def "should detect archaic header with DiskDude!" () {
        given:
        def headerBuffer = baseMarioBros()
                .put(BYTE_7, asciiBytes("DiskDude!"))
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == ARCHAIC_iNES
    }

    def "should detect modern header"() {
        given:
        def headerBuffer = baseMarioBros()
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == MODERN_iNES
    }

    def "should detect 2.0 header without size checks" () {
        given:
        def headerBuffer = baseMarioBros()
                .put(BYTE_7, ubyte(0b0000_1000)) // 0b10__ == 2.0
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == NES_2_0
    }

    static def baseMarioBros() {
        ByteBuffer header = headerBuffer()

        NesMeta marioBros = NesMetaBuilder.marioBros().build()

        NesHeader.Archaic_iNES.putMagic(header)
        NesHeader.Archaic_iNES.putProgramData(header, marioBros.programData())
        NesHeader.Archaic_iNES.putVideoData(header, marioBros.videoData().size())
        NesHeader.Archaic_iNES.putByte6(header, marioBros)
        // bytes 7-15 are 0s

        header
    }

    static def asciiBytes(String s) {
        s.getBytes(US_ASCII)
    }

    static def detectVersion(ByteBuffer headerBuffer) {
        new NesHeaderReader().detectVersion(headerBuffer)
    }

    // endregion

    static def headerBuffer() {
        ByteBuffer.allocate(NesHeader.SIZE)
    }

    def "should pass with correct magic bytes" () {
        given:
        def headerBuffer = headerBuffer()

        NesHeader.Archaic_iNES.putMagic(headerBuffer)
            .position(0)

        List<NesFileReader.Problem> problems = []

        when:
        new NesHeaderReader().readMagicNumber(problems, headerBuffer)

        then:
        problems.isEmpty()
    }

    def "should report with invalid magic bytes" () {
        given:
        def headerBuffer = headerBuffer()

        NesHeader.Archaic_iNES.putMagic(headerBuffer)
        headerBuffer.put(1, ubyte(0x46)) // 'F'
                .position(0)

        List<NesFileReader.Problem> problems = []

        when:
        new NesHeaderReader().readMagicNumber(problems, headerBuffer)

        then:
        // NesFileReadingException e = thrown()
        // e.cause == null
        // e.message == "Invalid magic bytes: 4e 46 53 1a"
        problems.size() == 1
        problems.get(0) == new NesFileReader.Problem(
                NesFileReader.Severity.MINOR,
                "More than 75% of magic number is matching: 4e 46 53 1a"
        )
    }
}
