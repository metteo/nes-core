package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.MagicNumber
import net.novaware.nes.core.file.NesData
import net.novaware.nes.core.file.NesFile
import net.novaware.nes.core.file.NesHash
import net.novaware.nes.core.file.NesMeta
import net.novaware.nes.core.util.Quantity
import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import java.nio.ByteBuffer

import static net.novaware.nes.core.file.NesMeta.Layout.ALTERNATIVE_VERTICAL
import static net.novaware.nes.core.file.NesMeta.Layout.STANDARD_HORIZONTAL
import static net.novaware.nes.core.file.NesMeta.System.EXTENDED
import static net.novaware.nes.core.file.NesMeta.System.VS_SYSTEM
import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC
import static net.novaware.nes.core.file.NesMeta.VideoStandard.PAL
import static net.novaware.nes.core.file.NesFileBuilder.marioBros
import static net.novaware.nes.core.util.Quantity.Unit.*

class NesFileWriterSpec extends Specification {

    def "should write mario bros file"() {
        given:
        NesFile marioBros = marioBros().build()

        def headerSize = NesHeader.SIZE // TODO: check actual header size :)
        def programSize = marioBros.meta().programData().toBytes()
        def videoSize = marioBros.meta().videoData().size().toBytes()

        def headerStart = 0
        def programStart = headerStart + headerSize
        def videoStart = programStart + programSize
        def videoEnd = videoStart + videoSize - 1

        def writer = new NesFileWriter()

        when:
        def buffer = writer.write(marioBros, NesHeader.Version.MODERN_iNES)

        then:
        buffer.limit() == headerSize + programSize + videoSize

        def maybeMagic = new byte[4]
        buffer.get(maybeMagic)

        maybeMagic == MagicNumber.GAME_NES.numbers()

        buffer.get(4) == 1 as byte // 16KB PRG
        buffer.get(5) == 1 as byte // 8KB CHR

        def maybeZeroes = new byte[10]
        buffer.get(6, maybeZeroes)

        verifyEach(maybeZeroes.toList()) {it == 0}

        buffer.get(programStart) == marioBros.data().program().get(0)
        buffer.get(videoStart - 1) == marioBros.data().program().get(programSize - 1)

        buffer.get(videoStart) == marioBros.data().video().get(0)
        buffer.get(videoEnd) == marioBros.data().video().get(videoSize - 1)
    }

    // TODO: write small test with single bit assertions to check if they set / unset correctly
    // TODO: write a test that verifies all Data section a in a proper place

    def "should write simple iNES file"() {

        // TODO: create a test builder for NesFile object
        given:
        def meta = NesMeta.builder()
            .noTitle()
            .noInfo()
            .system(EXTENDED)
            .mapper(53)
            .busConflicts(false)
            .noProgramMemory()
            .trainer(new Quantity(1, BANK_512B))
            .programData(new Quantity(3, BANK_16KB))
            .noVideoMemory()
            .videoData(new NesMeta.VideoData(STANDARD_HORIZONTAL, new Quantity(2, BANK_8KB)))
            .videoStandard(NTSC)
            .noFooter()
            .build()

        def data = new NesData(
            UByteBuffer.empty(),
            ByteBuffer.allocate(0),
            ByteBuffer.allocate(3 * 16384),
            ByteBuffer.allocate(2 * 8192),
            ByteBuffer.allocate(0),
            ByteBuffer.allocate(0)
        )

        def nesFile = new NesFile(URI.create("file://test.nes"), meta, data, NesHash.empty())
        def writer = new NesFileWriter()

        when:
        def buffer = writer.write(nesFile, NesHeader.Version.MODERN_iNES)


        then:
        buffer.limit() == 16 + 3 * 16384 + 2 * 8192

        // Header check
        buffer.get(0) == (byte) 'N'
        buffer.get(1) == (byte) 'E'
        buffer.get(2) == (byte) 'S'
        buffer.get(3) == 0x1A as byte

        buffer.get(4) == 3 as byte // 16KB PRG
        buffer.get(5) == 2 as byte // 8KB CHR

        // Flags 6:      0bMMMM_atbm where M - mapper lo, a - alt. mirroring, t - trainer, b - battery, m - mirroring
        buffer.get(6) == 0b0101_0101 as byte

        // Flags 7:      0bMMMM_NNPV where M - mapper hi, NN - NES 2.0, P - playchoice-10, V - vs. system
        buffer.get(7) == 0b0011_0011 as byte
        
        // Flags 9:      0b0000_000T where T - TV System (0 - NTSC, 1 - PAL)
        buffer.get(9) == 0b0000_0000 as byte
    }

    // FIXME: get rid of duplication like this. test object builders and fixtures are way to go.
    def "should write another simple iNES file"() {
        given:
        def meta = NesMeta.builder()
                .noTitle()
                .noInfo()
                .system(VS_SYSTEM)
                .mapper(202)
                .busConflicts(false)
                .noTrainer()
                .programMemory(new NesMeta.ProgramMemory(
                        NesMeta.Kind.PERSISTENT, new Quantity(252, BANK_8KB)
                ))
                .programData(new Quantity(3, BANK_16KB))
                .noVideoMemory()
                .videoData(new NesMeta.VideoData(ALTERNATIVE_VERTICAL, new Quantity(2, BANK_8KB)))
                .videoStandard(PAL)
                .noFooter()
                .build()

        def data = new NesData(
                UByteBuffer.empty(),
                ByteBuffer.allocate(0),
                ByteBuffer.allocate(3 * 16384),
                ByteBuffer.allocate(2 * 8192),
                ByteBuffer.allocate(0),
                ByteBuffer.allocate(0)
        )

        def nesFile = new NesFile(URI.create("file://test.nes"), meta, data, NesHash.empty())
        def writer = new NesFileWriter()

        when:
        def buffer = writer.write(nesFile, NesHeader.Version.MODERN_iNES)

        then:
        buffer.limit() == 16 + 3 * 16384 + 2 * 8192

        // Header check
        buffer.get(0) == (byte) 'N'
        buffer.get(1) == (byte) 'E'
        buffer.get(2) == (byte) 'S'
        buffer.get(3) == 0x1A as byte

        buffer.get(4) == 3 as byte // 16KB PRG
        buffer.get(5) == 2 as byte // 8KB CHR

        // Flags 6:      0bMMMM_atbm where M - mapper lo, a - alt. mirroring, t - trainer, b - battery, m - mirroring
        buffer.get(6) == 0b1010_1010 as byte

        // Flags 7:      0bMMMM_NNPV where M - mapper hi, NN - NES 2.0, P - playchoice-10, V - vs. system
        buffer.get(7) == 0b1100_0001 as byte

        // Flags 9:      0b0000_000T where T - TV System (0 - NTSC, 1 - PAL)
        buffer.get(9) == 0b0000_0001 as byte
    }

    def "should throw exception when nesFile is null"() {
        given:
        def writer = new NesFileWriter()

        when:
        writer.write(null, NesHeader.Version.MODERN_iNES)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "nesFile must not be null"
    }
}
