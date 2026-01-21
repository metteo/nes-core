package net.novaware.nes.core.file.ines

import net.novaware.nes.core.file.NesFile
import net.novaware.nes.core.file.NesFileBuilder
import net.novaware.nes.core.file.ReaderMode
import spock.lang.Specification

import java.nio.ByteBuffer

import static net.novaware.nes.core.file.NesFileBuilder.marioBros
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC

class NesFileHandlerSpec extends Specification {

    def "should write and then read back an archaic variant" () {
        given:
        NesFileBuilder archaicBuilder = marioBros()
        NesFileVersion version = ARCHAIC

        // TODO: maybe should be part of the builder
        archaicBuilder.data()
                .header(new NesHeaderWriter().write(archaicBuilder.meta().build(), new NesHeaderWriter.Params(version, true)).header())
                .footer(new NesFooterWriter().write("Mario Bros.", 127))

        NesFile archaicFile = archaicBuilder.build()

        when:
        ByteBuffer fileBuffer = new NesFileWriter().write(archaicFile, new NesFileWriter.Params(version, true, true))

        NesFileReader.Result result = new NesFileReader().read(
                archaicFile.origin(),
                new ByteArrayInputStream(fileBuffer.array()),
                ReaderMode.LENIENT
        )

        then:
        archaicFile == result.nesFile()
        // Check data sections
    }
}
