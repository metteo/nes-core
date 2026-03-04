package net.novaware.nes.core

import net.novaware.nes.core.clock.ClockMode
import net.novaware.nes.core.config.ImmutableCoreConfig
import net.novaware.nes.core.memory.MemoryBus
import spock.lang.Specification

class BoardCT extends Specification {

    def "should run nestest for a few seconds"() {
        given:
        BoardFactory factory = BoardFactory.newBoardFactory(ImmutableCoreConfig.builder()
                .setCpuBusType(MemoryBus.Type.STANDARD)
                .build(),
                ClockMode.LOOP
        )

        when:
        def board = factory.newBoard()

        then:
        board != null // no errors
    }
}
