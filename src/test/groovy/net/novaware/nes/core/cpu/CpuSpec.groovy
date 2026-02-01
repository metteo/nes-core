package net.novaware.nes.core.cpu

import net.novaware.nes.core.cpu.register.CpuRegisterFile
import net.novaware.nes.core.cpu.unit.ArithmeticLogic
import net.novaware.nes.core.memory.SystemBus
import spock.lang.Specification

import static net.novaware.nes.core.util.Bin.s
import static net.novaware.nes.core.util.UnsignedTypes.ubyte
import static net.novaware.nes.core.util.UnsignedTypes.ushort

class CpuSpec extends Specification {

    CpuRegisterFile registers = new CpuRegisterFile();
    SystemBus systemBus = new SystemBus();

    def "should construct an instance" () {
        when:
        newCpu()

        then:
        Cpu.RESET_VECTOR == ushort(0xFFFC)
    }

    Cpu newCpu() {
        def cpu = new Cpu(registers, systemBus, new ArithmeticLogic(registers))
        cpu.initialize()
        cpu.powerOn()
        cpu.reset()
        cpu
    }

    def "should jump to start of rom" () {
        given:
        systemBus.specifyAnd(Cpu.RESET_VECTOR).writeByte(ubyte(0x00))
        systemBus.specifyAnd(ushort(0xFFFD)).writeByte(ubyte(0x80))

        systemBus.specifyAnd(ushort(0x0345)).writeByte(ubyte(0xEA))

        when:
        def cpu = newCpu()

        then:
        registers.programCounter.get() == ushort(0x8000)
    }

    def "should calculate bitwise or"() {
        given:
        systemBus.specifyAnd(ushort(0x0000)).writeByte(ubyte(0x0D))
        systemBus.specifyAnd(ushort(0x0001)).writeByte(ubyte(0x54))
        systemBus.specifyAnd(ushort(0x0002)).writeByte(ubyte(0x06))
        systemBus.specifyAnd(ushort(0x0654)).writeByte(ubyte(0b1010_0110))

        when:
        def cpu = newCpu()
        def startCycle = registers.cycleCounter.getValue();

        registers.accumulator.set(ubyte(0b0101_1001))

        cpu.fetch()
        // TODO: check what was fetched
        cpu.decode()
        // TODO: check what was decoded
        cpu.execute()

        then:

        //TODO: registers.cycleCounter.getValue() == startCycle + 4

        registers.accumulator.get() == ubyte(0b1111_1111)
        //                            0bNV1B_DIZC
        s(registers.status.get()) == "0b1010_0100"
    }
}
