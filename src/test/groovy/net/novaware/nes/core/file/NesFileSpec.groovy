package net.novaware.nes.core.file


import spock.lang.Specification

class NesFileSpec extends Specification {

    def "should persist origin of the file" () {
        given:
        def expectedOrigin = "/home/user/file.nes"

        def magic = new byte[] { 0x4E, 0x45, 0x53, 0x1A }
        def bytes = new byte[16]
        System.arraycopy(magic, 0, bytes, 0, magic.length)

        def inputStream = new ByteArrayInputStream(bytes) // magic

        when:
        def file = new NesFileReader().read(expectedOrigin, inputStream)

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
        def nesFile = new NesFileReader().read("http://roms.pl", inputStream)

        then:
        nesFile.mirroring == NesFile.Mirroring.HORIZONTAL
        nesFile.mapper == (short) 15
        nesFile.getProgramData().capacity() == params.programRomSize
        nesFile.getVideoData().capacity() == params.videoRomSize

        nesFile.getLegacyHeader().capacity() == 16
        nesFile.getLegacyHeader().get(0) == (byte) 0x4E
        nesFile.getLegacyHeader().get(15) == (byte) 0

        nesFile.getTrainerData().capacity() == 0

        nesFile.getProgramData().capacity() == params.programRomSize
        nesFile.getProgramData().get(0) == fakeRom.programRomStart
        nesFile.getProgramData().get(params.programRomSize - 1) == fakeRom.programRomEnd

        nesFile.getVideoData().capacity() == params.videoRomSize
        nesFile.getVideoData().get(0) == fakeRom.videoRomStart
        nesFile.getVideoData().get(params.videoRomSize - 1) == fakeRom.videoRomEnd
    }
}
