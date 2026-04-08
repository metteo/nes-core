package net.novaware.nes.core.easy

import net.novaware.nes.core.easy.memory.EasyBus
import net.novaware.nes.core.util.Hex
import spock.lang.Specification

class EasyBoardCT extends Specification {

    def "should instantiate"() {
        given:
        EasyComp comp = EasyComp.newEasyComp()

        when:
        EasyBoard board = comp.newEasyBoard()
        board.preload(Hex.b(EasySnake.HEX))
        board.powerOn(false)

        Thread.sleep(500) // TODO: await would be better

        board.powerOff()

        then:
        ((EasyBus)board.bus).cycleCounter.getValue() > 100

    }
}
