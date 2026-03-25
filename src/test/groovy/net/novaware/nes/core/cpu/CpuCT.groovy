package net.novaware.nes.core.cpu

import net.novaware.nes.core.cpu.signal.Signal
import net.novaware.nes.core.util.RegsAndRamBaseSpec

class CpuCT extends RegsAndRamBaseSpec {

    def "should start at reset vector"() {
        given:
        ram(
            0xFFFC, 0x34,
            0xFFFD, 0x12
        )

        def cpu = factory.newCpu()

        when:
        cpu.initialize()
        cpu.res(Signal.LOW)
        cpu.advance()

        then:
        expectRegs(
            pc: 0x1234 + 1
        )
    }

    def "should execute jsr abs in 6 cycles"() {
        given:
        ram(
            0x1234, 0x20, // JSR
            0x1235, 0x2D,
            0x1236, 0xC7,
            0x1237, 0xEA, // NOP

            0xC72D, 0x60, // RTS

            0xFFFC, 0x34,
            0xFFFD, 0x12
        )

        def cpu = factory.newCpu()

        when:
        cpu.initialize()
        cpu.res(Signal.LOW)
        cpu.advance()
        cpu.res(Signal.HIGH)
        rec.record()
        cpu.advance()

        then:
        rec.cycleCounter().diff() == 6
        expectRegs(
            pc: 0xC72D + 1
        )

        and:
        rec.record()
        cpu.advance()

        then:
        rec.cycleCounter().diff() == 6
        expectRegs(
            pc: 0x1237 + 1
        )

        and:
        cpu.advance()
    }
}
