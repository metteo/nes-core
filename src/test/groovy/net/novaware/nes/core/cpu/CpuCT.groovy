package net.novaware.nes.core.cpu

import net.novaware.nes.core.BoardFactory
import net.novaware.nes.core.clock.LoopedClockGenerator
import spock.lang.Specification

import static net.novaware.nes.core.util.UnsignedTypes.ubyte
import static net.novaware.nes.core.util.UnsignedTypes.ushort

class CpuCT extends Specification {

    def "should execute simple adding loop"() {
        given:
        def board = BoardFactory.newBoardFactory(new LoopedClockGenerator()).newBoard()

        def cpu = board.cpu
        def bus = cpu.mmu.memoryBus

        bus.specifyThen(ushort(0xFFFC)).writeByte(ubyte(0x34))
        bus.specifyThen(ushort(0xFFFD)).writeByte(ubyte(0x12))

        when:
        cpu.initialize()

        then:
        cpu.registers.pc().getAsInt() == 0x1234 + 1
    }
}
