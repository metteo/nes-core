package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.ProgramMemoryBuilder
import net.novaware.nes.core.util.UByteBuffer
import spock.lang.Specification

import static net.novaware.nes.core.file.MagicNumber.GAME_NES
import static net.novaware.nes.core.file.NesMeta.Layout.ALTERNATIVE_HORIZONTAL
import static net.novaware.nes.core.file.NesMeta.Layout.ALTERNATIVE_VERTICAL
import static net.novaware.nes.core.file.NesMeta.System.EXTENDED
import static net.novaware.nes.core.file.NesMeta.System.NES
import static net.novaware.nes.core.file.NesMeta.System.PLAY_CHOICE_10
import static net.novaware.nes.core.file.NesMeta.System.VS_SYSTEM
import static net.novaware.nes.core.file.NesMeta.VideoStandard.*
import static net.novaware.nes.core.file.NesMetaBuilder.marioBros
import static net.novaware.nes.core.file.ProgramMemoryBuilder.battery8kb
import static net.novaware.nes.core.file.ProgramMemoryBuilder.volatile8kb
import static net.novaware.nes.core.file.VideoDataBuilder.horizontal
import static net.novaware.nes.core.file.VideoDataBuilder.videoData
import static net.novaware.nes.core.file.ines.ArchaicHeaderBuffer.*
import static net.novaware.nes.core.file.ines.FutureHeaderBuffer.BYTE_11
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.BYTE_10
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.BYTE_7
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.BYTE_8
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.BYTE_9
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC_0_7
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_3
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_5
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_7
import static net.novaware.nes.core.util.Bin.s
import static net.novaware.nes.core.util.QuantityBuilder.banks16kb
import static net.novaware.nes.core.util.QuantityBuilder.banks512b

class NesHeaderWriterSpec extends Specification {

    static final def ZERO = "0b0000_0000";

    NesHeaderWriter writer;

    def setup() {
        writer = new NesHeaderWriter()
    }

    def "should write archaic header" () {
        given:
        def meta = marioBros()
                .mapper(9)
                .trainer(banks512b(1))
                .videoData(horizontal(2))
                .build()

        def params = new NesHeaderWriter.Params(ARCHAIC, false)

        when:
        def header = writer.write(meta, params).header()

        then:
        header.capacity() == 16

        getMagic(header) == GAME_NES.numbers()

        header.getAsInt(BYTE_4) == 1
        header.getAsInt(BYTE_5) == 2

        //                        0bMMMM_atbm - Mapper lo, Alt. mirroring, Trainer, Battery, (m)irroring
        s(header.get(BYTE_6)) == "0b1001_0101"

        verifyEach(getRemainingData(BYTE_7, header).toList()) {it == 0 }
    }

    def "should write iNES 0.7 header" () {
        given:
        def meta = marioBros()
                .mapper(89)
                .programMemory(battery8kb(1))
                .programData(banks16kb(3))
                .videoData(videoData(ALTERNATIVE_VERTICAL, 5))
                .build()

        def params = new NesHeaderWriter.Params(ARCHAIC_0_7, false)

        when:
        def header = writer.write(meta, params).header()

        then:
        header.capacity() == 16

        getMagic(header) == GAME_NES.numbers()

        header.getAsInt(BYTE_4) == 3
        header.getAsInt(BYTE_5) == 5

        //                        0bMMMM_atbm - Mapper lo, Alt. mirroring, Trainer, Battery, (m)irroring
        s(header.get(BYTE_6)) == "0b1001_1010"

        //                        0bMMMM_0000 - Mapper hi
        s(header.get(BYTE_7)) == "0b0101_0000"

        verifyEach(getRemainingData(BYTE_8, header).toList()) {it == 0 }

    }

    def "should write modern & modern 1.3 header" () {
        given:
        def meta = marioBros()
                .mapper(166)
                .system(system)
                .programData(banks16kb(8))
                .videoData(videoData(ALTERNATIVE_HORIZONTAL, 10))
                .build()

        def params = new NesHeaderWriter.Params(version, false)

        when:
        def header = writer.write(meta, params).header()

        then:
        header.capacity() == 16

        getMagic(header) == GAME_NES.numbers()

        header.getAsInt(BYTE_4) == 8
        header.getAsInt(BYTE_5) == 10

        //                        0bMMMM_atbm - Mapper lo, Alt. mirroring, Trainer, Battery, (m)irroring
        s(header.get(BYTE_6)) == "0b0110_1001"

        //                        0bMMMM_00" + "ss" - Mapper hi, ss - system (nes / vs / playchoice)
        s(header.get(BYTE_7)) == "0b1010_00" + systemBits

        verifyEach(getRemainingData(BYTE_8, header).toList()) {it == 0 }

        where:
        version    | system         || systemBits
        MODERN     | NES            || "00"
        MODERN     | VS_SYSTEM      || "01"
        MODERN     | PLAY_CHOICE_10 || "00"
        MODERN     | EXTENDED       || "00"

        MODERN_1_3 | NES            || "00"
        MODERN_1_3 | VS_SYSTEM      || "01"
        MODERN_1_3 | PLAY_CHOICE_10 || "10"
        MODERN_1_3 | EXTENDED       || "00" // NES 2.0 added support for 0b11
    }

    def "should write modern 1.5 header" () {
        given:
        def meta = marioBros()
                .mapper(166)
                .programMemory(volatile8kb(programMemory))
                .programData(banks16kb(11))
                .videoData(videoData(ALTERNATIVE_HORIZONTAL, 13))
                .videoStandard(videoStandard)
                .build()

        def params = new NesHeaderWriter.Params(MODERN_1_5, false)

        when:
        def header = writer.write(meta, params).header()

        then:
        header.capacity() == 16

        getMagic(header) == GAME_NES.numbers()

        with (header) {
            getAsInt(BYTE_4) == 11
            getAsInt(BYTE_5) == 13

            //                 0bMMMM_atbm - Mapper lo, Alt. mirroring, Trainer, Battery, (m)irroring
            s(get(BYTE_6)) == "0b0110_1001"

            //                 0bMMMM_00ss - Mapper hi, s - system (nes / vs / playchoice)
            s(get(BYTE_7)) == "0b1010_0000"

            s(get(BYTE_8)) == byte8
            s(get(BYTE_9)) == byte9
        }
        verifyEach(getRemainingData(BYTE_10, header).toList()) {it == 0 }

        where:
        programMemory | videoStandard | byte8         | byte9
    //                                  "0bmmmm_mmmm" | "0b0000_000v", m - prgMem size, v - video standard
        0             | NTSC          | ZERO          | "0b0000_0000"
        0             | PAL           | ZERO          | "0b0000_0001"
        0             | NTSC_DUAL     | ZERO          | "0b0000_0000"
        0             | PAL_DUAL      | ZERO          | "0b0000_0001"
        0             | DENDY         | ZERO          | "0b0000_0000" // ignore dendy
        0             | UNKNOWN       | ZERO          | "0b0000_0000" // ignore unknown

        1             | NTSC          | "0b0000_0001" | ZERO
        4             | NTSC          | "0b0000_0100" | ZERO
    //  4x 8KB is usual max
    }

    def "should write modern 1.7 header" () {
        given:
        def meta = marioBros()
                .mapper(166)
                .programMemory(prgMemAbsent
                        ? ProgramMemoryBuilder.none()
                        : volatile8kb(0)
                )
                .programData(banks16kb(14))
                .videoData(videoData(ALTERNATIVE_HORIZONTAL, 15))
                .videoStandard(videoStandard)
                .busConflicts(busConflicts)
                .build()

        def params = new NesHeaderWriter.Params(MODERN_1_7, false)

        when:
        def header = writer.write(meta, params).header()

        then:
        header.capacity() == 16

        getMagic(header) == GAME_NES.numbers()

        with (header) {
            getAsInt(BYTE_4) == 14
            getAsInt(BYTE_5) == 15

            s(get(BYTE_6)) == "0b0110_1001"
            s(get(BYTE_7)) == "0b1010_0000"
            s(get(BYTE_8)) == ZERO
            s(get(BYTE_9)) == byte9
            s(get(BYTE_10)) == byte10
        }

        verifyEach(getRemainingData(BYTE_11, header).toList()) {it == 0 }

        where:
        busConflicts | prgMemAbsent | videoStandard | byte9         | byte10
    //                                     v - video standard basic | c - bus confl., m - prg mem absence, V - vidStd ext
    //                                                "0b0000_000v" | "0b00cm_00VV"
        false        | false        | NTSC          | "0b0000_0000" | "0b0000_0000"
        false        | false        | PAL           | "0b0000_0001" | "0b0000_0010"
        false        | false        | NTSC_DUAL     | "0b0000_0000" | "0b0000_0001"
        false        | false        | PAL_DUAL      | "0b0000_0001" | "0b0000_0011"

        true         | false        | NTSC          | "0b0000_0000" | "0b0010_0000"
        false        | true         | NTSC          | "0b0000_0000" | "0b0001_0000"
    }

    def "should throw on null version" () {
        given:
        def writer = new NesHeaderWriter()

        when:
        writer.write(marioBros().build(), new NesHeaderWriter.Params(null, false))

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "version must not be null"
    }

    def "should throw on null params" () {
        given:
        def writer = new NesHeaderWriter()

        when:
        writer.write(marioBros().build(), null)


        then:
        def e = thrown(IllegalArgumentException)
        e.message == "params must not be null"
    }

    def "should throw on null meta" () {
        given:
        def writer = new NesHeaderWriter()

        when:
        writer.write(null, new NesHeaderWriter.Params(ARCHAIC, false))


        then:
        def e = thrown(IllegalArgumentException)
        e.message == "meta must not be null"
    }

    static byte[] getRemainingData(int startByte, UByteBuffer header) {
        def maybeZeroes = new byte[NesHeader.SIZE - startByte]
        header.get(startByte, maybeZeroes)
        maybeZeroes
    }

    static byte[] getMagic(UByteBuffer header) {
        def maybeMagic = new byte[4]
        header.get(BYTE_0, maybeMagic)
        maybeMagic
    }
}
