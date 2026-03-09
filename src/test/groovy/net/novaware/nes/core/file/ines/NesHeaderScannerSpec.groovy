package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.NesMeta
import net.novaware.nes.core.file.NesMetaBuilder
import net.novaware.nes.core.file.Problem
import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.US_ASCII
import static net.novaware.nes.core.file.MagicNumber.GAME_NES
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.BYTE_7
import static net.novaware.nes.core.file.ines.NesFileVersion.*
import static net.novaware.nes.core.util.UTypes.ubyte

class NesHeaderScannerSpec extends Specification {

    // region detectVersion

    def "should detect archaic header with D at byte 7" () {
        given:
        def headerBuffer = baseMarioBros()
                .put(BYTE_7, asciiBytes("D"))
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == ARCHAIC
    }

    def "should detect archaic header with DiskDude!" () {
        given:
        def headerBuffer = baseMarioBros()
                .put(BYTE_7, asciiBytes("DiskDude!"))
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == ARCHAIC
    }

    def "should detect modern header"() {
        given:
        def headerBuffer = baseMarioBros()
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == MODERN
    }

    def "should detect 2.0 header without size checks" () {
        given:
        def headerBuffer = baseMarioBros()
                .put(BYTE_7, ubyte(0b0000_1000)) // 0b10__ == 2.0
                .position(BYTE_7)

        when:
        def version = detectVersion(headerBuffer)

        then:
        version == FUTURE
    }

    static UByteBuffer baseMarioBros() {
        def header = headerBuffer()

        NesMeta marioBros = NesMetaBuilder.marioBros().build()

        new ArchaicHeaderBuffer(header)
                .putMagic()
                .putProgramData(marioBros.programData())
                .putVideoData(marioBros.videoData().size())
                .putTrainer(marioBros.trainer())
                .putMapper(ARCHAIC_0_7, marioBros.mapper())
                .putProgramMemoryKind(marioBros.programMemory().kind())
                .putVideoMemoryLayout(marioBros.videoData().layout())
        // bytes 7-15 are 0s

        header
    }

    static def asciiBytes(String s) {
        s.getBytes(US_ASCII)
    }

    static def detectVersion(UByteBuffer headerBuffer) {
        new NesHeaderScanner().detectVersion(headerBuffer)
    }

    // endregion

    static def headerBuffer() {
        UByteBuffer.allocate(NesHeader.SIZE)
    }

    def "should pass with correct magic bytes" () {
        given:
        def headerBuffer = headerBuffer()

        new ArchaicHeaderBuffer(headerBuffer).putMagic()

        List<Problem> problems = []

        when:
        new NesHeaderScanner().detectMagicNumber(problems, headerBuffer)

        then:
        problems.isEmpty()
    }

    def "should report with invalid magic bytes" () {
        given:
        def headerBuffer = headerBuffer()

        new ArchaicHeaderBuffer(headerBuffer).putMagic()
        headerBuffer.put(1, ubyte(0x46)) // 'F'
                .position(0)

        List<Problem> problems = []

        when:
        new NesHeaderScanner().detectMagicNumber(problems, headerBuffer)

        then:
        // NesFileReadingException e = thrown()
        // e.cause == null
        // e.message == "Invalid magic bytes: 4e 46 53 1a"
        problems.size() == 1
        problems.get(0) == new Problem(
                Problem.Severity.MINOR,
                "More than 75% of magic number is matching: 4e 46 53 1a"
        )
    }

    def "should scan header and report result" () {
        given:
        def headerBuffer = baseMarioBros()
                .position(0)

        when:
        def result = new NesHeaderScanner().scan(headerBuffer)

        then:
        result == new NesHeaderScanner.Result(GAME_NES, MODERN, [])
    }
}
