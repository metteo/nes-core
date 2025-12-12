package net.novaware.nes.core.file

import spock.lang.Specification

class NesFileSpec extends Specification {

    def "should persist origin of the file" () {
        given:
        def expectedOrigin = "/home/user/file.nes"
        def inputStream = new ByteArrayInputStream(new byte[] {0x4E, 0x45, 0x53, 0x1A}) // magic

        when:
        def file = new NesFile(expectedOrigin, inputStream)

        then:
        file.origin == expectedOrigin
    }
}
