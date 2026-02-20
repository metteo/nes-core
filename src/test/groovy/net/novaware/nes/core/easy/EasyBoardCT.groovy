package net.novaware.nes.core.easy

import net.novaware.nes.core.util.Hex
import spock.lang.Specification

class EasyBoardCT extends Specification {

    def "should instantiate"() {
        given:
        EasyComp comp = EasyComp.newEasyComp()

        // 3 pixels in 3 colors: https://skilldrick.github.io/easy6502/#first-program
        def data = Hex.b("a9 01 8d 00 02 a9 05 8d 01 02 a9 08 8d 02 02")

        when:
        EasyBoard board = comp.newEasyBoard()
        board.preload(data)
        board.powerOn()

        Thread.sleep(5000) // TODO: await would be better
        board.powerOff()

        then:
        board != null // no errors
    }
}
