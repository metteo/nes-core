package net.novaware.nes.core.dma

import net.novaware.nes.core.TestNesCore
import net.novaware.nes.core.cpu.Cpu
import net.novaware.nes.core.cpu.signal.Signal
import net.novaware.nes.core.dma.inject.DmaRegModule
import net.novaware.nes.core.memory.BusOp
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_OAM_ADDRESS_REGISTER
import static net.novaware.nes.core.dma.Dma.State.*
import static net.novaware.nes.core.util.UTypes.ubyte

class DmaSpec extends Specification {

    def oamDma = DmaRegModule.provideOamDmaRegister()

    dagger.Lazy<Cpu> lazyCpu = Mock()
    def cpu = Mock(Cpu)

    def cpuBus = TestNesCore.newTestNesCore().newCpuBus()

    def "should construct an instance"() {
        when:
        def instance = newDma()

        then:
        instance.getState() == IDLE

        and:
        def consumed = instance.cycle()

        then:
        consumed == 0
        0 * lazyCpu._
    }

    def "should change state on trigger"() {
        given:
        def dma = newDma()
        oamDma.set(ubyte(0x20))

        when:
        dma.triggerDma()

        then:
        dma.getState() == HALT
        dma.oamDma.getAsInt() == 0x20
        dma.offset.getAsInt() == 0x00
    }

    def "should align bus access when halted unaligned"() {
        given:
        def dma = newDma()
        dma.state = HALT
        oamDma.set(ubyte(0x20))
        cpuBus.busOp = BusOp.DATA_WRITE

        when:
        def consumed = dma.cycle()

        then:
        consumed == 1
        dma.state == ALIGN
        1 * lazyCpu.get() >> cpu
        1 * cpu.rdy(Signal.LOW)
        cpuBus.busOp == BusOp.DATA_READ
    }

    def "should read when halted aligned with bus"() {
        given:
        def dma = newDma()
        dma.state = HALT
        oamDma.set(ubyte(0x20))
        cpuBus.busOp = BusOp.DATA_READ

        when:
        def consumed = dma.cycle()

        then:
        consumed == 1
        dma.state == READ
        1 * lazyCpu.get() >> cpu
        1 * cpu.rdy(Signal.LOW)
        cpuBus.busOp == BusOp.DATA_WRITE
        cpuBus.access(PPU_OAM_ADDRESS_REGISTER).read().data() == ubyte(0x00)
    }

    // TODO: ALIGN state
    // TODO: READ state
    // TODO: WRITE non last state
    // TODO: WRITE last state

    def newDma() {
        new Dma(oamDma, lazyCpu, cpuBus)
    }
}
