package net.novaware.nes.core.cpu

import net.novaware.nes.core.BoardFactory
import net.novaware.nes.core.clock.ClockMode
import net.novaware.nes.core.config.ImmutableCoreConfig
import net.novaware.nes.core.cpu.signal.Signal
import net.novaware.nes.core.memory.MemoryBus
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class CpuCT extends Specification {

    def "should execute simple adding loop"() {
        given:
        BoardFactory factory = BoardFactory.newBoardFactory(ImmutableCoreConfig.builder()
                .setCpuBusType(MemoryBus.Type.STANDARD)
                .build(),
                ClockMode.LOOP
        )
        def board = factory.newBoard()

        def cpu = board.cpu
        def bus = cpu.mmu.memoryBus

        bus.specifyThen(ushort(0xFFFC)).writeByte(ubyte(0x34))
        bus.specifyThen(ushort(0xFFFD)).writeByte(ubyte(0x12))

        when:
        cpu.initialize()
        cpu.res(Signal.LOW)
        cpu.advance()

        then:
        cpu.registers.pc().getAsInt() == 0x1234 + 1
    }
}
