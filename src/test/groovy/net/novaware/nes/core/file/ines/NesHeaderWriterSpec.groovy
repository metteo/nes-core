package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.MagicNumber
import spock.lang.Specification

import static net.novaware.nes.core.file.NesMetaBuilder.marioBros
import static net.novaware.nes.core.file.VideoDataBuilder.horizontal
import static net.novaware.nes.core.file.ines.ArchaicHeaderBuffer.*
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.BYTE_7
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC_0_7
import static net.novaware.nes.core.util.QuantityBuilder.banks512b

class NesHeaderWriterSpec extends Specification {

    def "should write archaic header" () {
        given:
        def writer = new NesHeaderWriter()
        def meta = marioBros()
                .mapper(9)
                .trainer(banks512b(1))
                .videoData(horizontal(2))
                .build()
        def params = new NesHeaderWriter.Params(ARCHAIC_0_7, false)

        when:
        def header = writer.write(meta, params)

        then:
        header.capacity() == 16

        def maybeMagic = new byte[4]
        header.get(BYTE_0, maybeMagic)

        maybeMagic == MagicNumber.GAME_NES.numbers()

        header.getAsInt(BYTE_4) == 1
        header.getAsInt(BYTE_5) == 2

        //                         0bMMMM_atbm - Mapper lo, Alt. mirroring, Trainer, Battery, (m)irroring
        header.getAsInt(BYTE_6) == 0b1001_0101

        def maybeZeroes = new byte[9]
        header.get(BYTE_7, maybeZeroes)
        verifyEach(maybeZeroes.toList()) {it == 0 }
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
}
