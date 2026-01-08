package net.novaware.nes.core.file


import spock.lang.Specification

import static net.novaware.nes.core.file.NesFileReader.Mode.*

class NesFileSpec extends Specification {

    def "should persist origin of the file" () {
        given:
        def expectedOrigin = "/home/user/file.nes"

        def magic = new byte[] { 0x4E, 0x45, 0x53, 0x1A }
        def bytes = new byte[16]
        System.arraycopy(magic, 0, bytes, 0, magic.length)

        def inputStream = new ByteArrayInputStream(bytes) // magic

        when:
        def result = new NesFileReader().read(expectedOrigin, inputStream, LENIENT)

        then:
        result.nesFile().origin() == expectedOrigin
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
        def result = new NesFileReader().read("http://roms.pl", inputStream, STRICT)
        def nesFile = result.nesFile()
        def meta = nesFile.meta
        def data = nesFile.data



        then:
        meta.mirroring() == NesFile.Mirroring.HORIZONTAL
        meta.mapper() == (short) 15
        meta.trainer().amount() == 0
        meta.programData().amount() == params.programRomSize
        meta.videoData().amount() == params.videoRomSize

        data.header().capacity() == 16
        data.header().get(0) == (byte) 0x4E
        data.header().get(15) == (byte) 0

        data.trainer().capacity() == 0

        data.program().capacity() == params.programRomSize
        data.program().get(0) == fakeRom.programRomStart
        data.program().get(params.programRomSize - 1) == fakeRom.programRomEnd

        data.video().capacity() == params.videoRomSize
        data.video().get(0) == fakeRom.videoRomStart
        data.video().get(params.videoRomSize - 1) == fakeRom.videoRomEnd
    }
}
