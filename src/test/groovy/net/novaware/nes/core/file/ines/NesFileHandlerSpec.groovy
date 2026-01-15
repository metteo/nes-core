package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.NesFile
import net.novaware.nes.core.file.NesFileBuilder
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

import static net.novaware.nes.core.file.NesFileBuilder.marioBros
import static net.novaware.nes.core.file.ines.NesHeader.Version.ARCHAIC_iNES

class NesFileHandlerSpec extends Specification {

    def "should write and then read back an archaic variant" () {
        given:
        NesFileBuilder archaicBuilder = marioBros()
        NesHeader.Version version = ARCHAIC_iNES

        // TODO: maybe should be part of the builder
        archaicBuilder.data()
                .header(new NesHeaderWriter().write(new NesHeaderWriter.Params(version, true), archaicBuilder.meta().build()))
                .footer(new NesFooterWriter().write("Mario Bros.", 127))

        NesFile archaicFile = archaicBuilder.build()

        when:
        ByteBuffer fileBuffer = new NesFileWriter().write(archaicFile, version)

        NesFileReader.Result result = new NesFileReader().read(
                archaicFile.origin(),
                new ByteArrayInputStream(fileBuffer.array()),
                NesFileReader.Mode.LENIENT
        )

        then:
        archaicFile == result.nesFile()
        // Check data sections
    }
}
