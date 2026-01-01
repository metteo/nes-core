package net.novaware.nes.core.file

import net.novaware.nes.core.util.Quantity
import spock.lang.Specification

import static net.novaware.nes.core.util.Quantity.Unit.BYTES

class NesFileSpec extends Specification {

    def "should persist origin of the file" () {
        given:
        def expectedOrigin = "/home/user/file.nes"

        def magic = new byte[] { 0x4E, 0x45, 0x53, 0x1A }
        def bytes = new byte[16]
        System.arraycopy(magic, 0, bytes, 0, magic.length)

        def inputStream = new ByteArrayInputStream(bytes) // magic

        when:
        def file = new NesFile(expectedOrigin, inputStream)

        then:
        file.origin == expectedOrigin
    }

    def "should parse faked file"() {
        given:
        def faker = new NesFileFaker()
        def params = new NesFileFaker.Params(
                version: NesFileFaker.Version.iNES,
                trainerPresent: false,
                programRomSize: 3 * 16 * 1024, // bytes
                videoRomSize: 1 * 8 * 1024, // bytes
                nametable: NesFileFaker.Orientation.HORIZONTAL,
                mapper: 15
        )

        def fakeRom = faker.generate(params)
        def inputStream = new ByteArrayInputStream(fakeRom.fileData) // no close

        when:
        def nesFile = new NesFile("http://roms.pl", inputStream)
        def parsedHeader = nesFile.getHeader()

        then:
        //parsedHeader.nametableOrientation == NesFile.Orientation.HORIZONTAL // faker does not support
        //parsedHeader.mapperNumber == 15                                     // faker does not support
        parsedHeader.getProgramRomSize() == new Quantity(params.programRomSize, BYTES)
        parsedHeader.getVideoRomSize() == new Quantity(params.videoRomSize, BYTES)

        // TODO: check if boundaries are OK in prg & chr
    }
}
