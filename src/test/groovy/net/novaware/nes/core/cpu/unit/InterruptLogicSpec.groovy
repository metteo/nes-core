package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.register.Status

import static net.novaware.nes.core.cpu.signal.Signal.HIGH
import static net.novaware.nes.core.cpu.signal.Signal.LOW

class InterruptLogicSpec extends ControlUnitBaseSpec {

    def cpu = factory.newCpu()
    def interrupts = factory.newInterruptLogic()

    def "should react to brk"() {
        given:
        regs(
            pc: 0x1234,
            sp: 0xFD,
            i: initialIrqDisable // ignored by BRK
        )

        ram(
            0xFFFE, 0xCD,
            0xFFFF, 0xAB,
        )

        Status stackStatus = registers.status().get()

        bus.cycleCounter().mark()

        when:
        interrupts.forceBreak()
        interrupts.sample()

        then:
        bus.cycleCounter().diff() == 7 - 2 // opcode & ignored operand done in CU

        expectRegs(
            pc: 0xABCD,
            sp: 0xFD - 3,
            i: true
        )

        expectRam(
            0x01FB, stackEntry,  // ^
            0x01FC, 0x34,        // |
            0x01FD, 0x12         // |
        )

        // pushed
        stackStatus.isIrqDisabled() == initialIrqDisable
        stackStatus.getBreak()

        and: "return from it"
        interrupts.returnFromInterrupt()

        then:
        expectRegs(
            pc: 0x1234,
            sp: 0xFD,
            i: initialIrqDisable // immediately applied
        )

        // pulled
        stackStatus.isIrqDisabled() == initialIrqDisable
        stackStatus.getBreak()

        where:
        initialIrqDisable || stackEntry
        true              || 0b0011_0100
        false             || 0b0011_0000
        //                || 0bNV1B_DIZC
    }

    def "should react to nmi"() {
        given:
        regs(
            pc: 0x1234,
            sp: 0xFD,
            i: initialIrqDisable // ignored by NMI
        )

        ram(
            0xFFFA, 0xCD,
            0xFFFB, 0xAB,
        )

        Status stackStatus = registers.status().get()

        bus.cycleCounter().mark()

        when:
        cpu.nmi(LOW)
        interrupts.sample()
        cpu.nmi(HIGH)

        then:
        bus.cycleCounter().diff() == 7

        expectRegs(
            pc: 0xABCD,
            sp: 0xFD - 3,
            i: true
        )

        expectRam(
            0x01FB, stackEntry,  // ^
            0x01FC, 0x34,        // |
            0x01FD, 0x12         // |
        )

        // pushed
        stackStatus.isIrqDisabled() == initialIrqDisable
        !stackStatus.getBreak()

        and: "return from it"
        interrupts.returnFromInterrupt()

        then:
        expectRegs(
            pc: 0x1234,
            sp: 0xFD,
            i: initialIrqDisable // immediately applied
        )

        // pulled
        stackStatus.isIrqDisabled() == initialIrqDisable
        !stackStatus.getBreak()

        where:
        initialIrqDisable || stackEntry
        true              || 0b0010_0100
        false             || 0b0010_0000
        //                || 0bNV1B_DIZC
    }

    def "should react to irq"() {
        given:
        regs(
            pc: 0x1234,
            sp: 0xFD,
            i: false // respected by IRQ
        )

        ram(
            0xFFFE, 0xCD,
            0xFFFF, 0xAB,
        )

        Status stackStatus = registers.status().get()

        bus.cycleCounter().mark()

        when:
        cpu.irq(LOW)
        interrupts.sample()
        cpu.irq(HIGH)

        then:
        bus.cycleCounter().diff() == 7

        expectRegs(
            pc: 0xABCD,
            sp: 0xFD - 3,
            i: true
        )

        expectRam(
                //  0bNV1B_DIZC
            0x01FB, 0b0010_0000,  // ^
            0x01FC, 0x34,         // |
            0x01FD, 0x12          // |
        )

        // pushed
        !stackStatus.isIrqDisabled()
        !stackStatus.getBreak()

        and: "return from it"
        interrupts.returnFromInterrupt()

        then:
        expectRegs(
            pc: 0x1234,
            sp: 0xFD,
            i: false // immediately applied
        )

        // pulled
        !stackStatus.isIrqDisabled()
        !stackStatus.getBreak()
    }

    def "should ignore irq (when disabled)"() {
        given:
        regs(
            pc: 0x1234,
            sp: 0xFD,
            i: true // respected by IRQ
        )

        ram(
            0xFFFE, 0xCD,
            0xFFFF, 0xAB,
        )

        bus.cycleCounter().mark()

        when:
        cpu.irq(LOW)
        interrupts.sample()
        cpu.irq(HIGH)

        then:
        bus.cycleCounter().diff() == 0

        expectRegs(
            pc: 0x1234,
            sp: 0xFD,
            i: true
        )

        expectRam(
            0x01FB, 0,
            0x01FC, 0,
            0x01FD, 0
        )
    }
}
