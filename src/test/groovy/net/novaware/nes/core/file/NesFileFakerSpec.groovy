package net.novaware.nes.core.file

import spock.lang.Specification

import static net.novaware.nes.core.util.Hex.b

class NesFileFakerSpec extends Specification {

    def "should return correct file data with min options set"() {
        given:
        def params = new NesFileFaker.Params(
                version: NesFileFaker.Version.iNES,
                trainerPresent: false,
                nametable: NesFileFaker.Orientation.VERTICAL,
                mapper: 0,
                programRomSize: 1 * 16 * 1024,
                videoRomSize: 1 * 8 * 1024
        )

        when:
        def result = new NesFileFaker().generate(params)

        then:
        result.fileData.size() == 16 + 16 * 1024 + 8 * 1024
        result.fileData[0..3] as byte[] == b('4E 45 53 1A')
        result.fileData[4] == b(0x01)
        result.fileData[5] == b(0x01)
        result.fileData[6] == b(0)
        verifyEach(result.fileData[7..15]) {it == 0}

        result.fileData[16] == result.programRomStart
        result.fileData[16 + 16 * 1024 - 1] == result.programRomEnd

        result.fileData[16 + 16 * 1024] == result.videoRomStart
        result.fileData[16 + 16 * 1024 + 8 * 1024 - 1] == result.videoRomEnd
    }

    def "should return correct file data with all options set"() {
        given:
        def params = new NesFileFaker.Params(
                version: NesFileFaker.Version.iNES,
                trainerPresent: true,
                nametable: NesFileFaker.Orientation.HORIZONTAL,
                mapper: 12,
                programRomSize: 2 * 16 * 1024,
                videoRomSize: 1 * 8 * 1024
        )

        when:
        def result = new NesFileFaker().generate(params)

        then:
        result.fileData.size() == 16 + 512 + 32 * 1024 + 8 * 1024
        result.fileData[0..3] as byte[] == b('4E 45 53 1A')
        result.fileData[4] == b(0x02)
        result.fileData[5] == b(0x01)
        result.fileData[6] == b(0xC5)
        verifyEach(result.fileData[7..15]) {it == 0}

        result.fileData[16] == result.trainerStart
        result.fileData[16 + 512 - 1] == result.trainerEnd

        result.fileData[16 + 512] == result.programRomStart
        result.fileData[16 + 512 + 32 * 1024 - 1] == result.programRomEnd

        result.fileData[16 + 512 + 32 * 1024] == result.videoRomStart
        result.fileData[16 + 512 + 32 * 1024 + 8 * 1024 - 1] == result.videoRomEnd
    }
}
