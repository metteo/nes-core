package net.novaware.nes.core.file


import spock.lang.Specification

import static net.novaware.nes.core.util.Hex.b

class NesFileFakerSpec extends Specification {

    def "should return correct header"() {
        given:
        def params = new NesFileFaker.Params(
                version: NesFileFaker.Version.iNES,
                programRomSize: 32 * 1024,
                videoRomSize: 8 * 1024
        )

        when:
        def result = new NesFileFaker().generate(params)

        then:
        result.fileData.size() == 16 + 32 * 1024 + 8 * 1024
        result.fileData[0..3] as byte[] == b('4E 45 53 1A')
        result.fileData[4] == b(0x02)
        result.fileData[5] == b(0x01)
        verifyEach(result.fileData[6..15]) {it == 0}

        result.fileData[16] == result.programRomStart
        result.fileData[16 + 32 * 1024 - 1] == result.programRomEnd

        result.fileData[16 + 32 * 1024] == result.videoRomStart
        result.fileData[16 + 32 * 1024 + 8 * 1024 - 1] == result.videoRomEnd
    }
}
