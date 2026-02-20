package net.novaware.nes.core.cpu.unit

import net.novaware.nes.core.cpu.CpuRegisters
import net.novaware.nes.core.cpu.memory.MemoryMap
import net.novaware.nes.core.cpu.register.Status
import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.RecordingBus
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class StackEngineSpec extends Specification {

    CpuRegisters regs = new CpuRegisters()
    MemoryBus bus = new RecordingBus()
    MemoryMgmt mmu = new MemoryMgmt(regs, bus)
    StackEngine engine = new StackEngine(regs, mmu)

    def setup() {
        regs.getStackSegment().set(MemoryMap.STACK_SEGMENT_START)
    }

    def "should push accumulator on the stack"() {
        given:
        regs.a().setAsByte(0x12)
        regs.sp().setAsByte(0xFD)

        when:
        engine.push(regs.a())

        then:
        bus.specifyThen(ushort(0x01FD)).readByte() == ubyte(0x12)
        regs.sp().getAsInt() == 0xFC
    }

    def "should pull accumulator from the stack"() {
        given:
        regs.sp().setAsByte(0xFC)
        bus.specifyThen(ushort(0x01FD)).writeByte(ubyte(0x34))

        when:
        engine.pull(regs.a())

        then:
        regs.a().get() == ubyte(0x34)
        regs.sp().getAsInt() == 0xFD
    }

    def "should push processor status on the stack"() {
        given:
        regs.sp().setAsByte(0xFD)
        regs.status().initialize()

        when:
        engine.pushStatus()

        then:
        bus.specifyThen(ushort(0x01FD)).readByte() == ubyte(0b0011_0100)
        regs.sp().getAsInt() == 0xFC
    }

    def "should pull processor status from the stack"() {
        given:
        regs.sp().setAsByte(0xFC)
        regs.status()
            .setCarry(true)
            .setZero(true)
            .setIrqDisabled(false)
            .setDecimal(true)
            .setOverflow(true)
            .setNegative(true)

        bus.specifyThen(ushort(0x01FD)).writeByte(ubyte(0b0011_0100))

        Status s = regs.status().get() // we hold the reference to check break flag from the stack

        when:
        engine.pullStatus()

        then:
        !regs.status().getCarry()
        !regs.status().isZero()
        regs.status().isIrqDisabled()
        !regs.status().isDecimal()
        !regs.status().isOverflow()
        !regs.status().isNegative()

        !s.getCarry()
        !s.isZero()
        s.isIrqDisabled()
        !s.isDecimal()
        s.getBreak() // break was set
        !s.isOverflow()
        !s.isNegative()
    }
}
