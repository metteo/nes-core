package net.novaware.nes.core.cpu.memory


import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.register.CycleCounter
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.*
import static net.novaware.nes.core.util.UTypes.*

class CpuBusSpec extends Specification {

    def cycleCounter = new CycleCounter("CPUCC")
    def ram = new PhysicalMemory("RAM", RAM_START, RAM_MIRROR_END, RAM_SIZE)
    def ppu = new PhysicalMemory("PPU", PPU_REGISTERS_START, PPU_REGISTERS_END, PPU_REGISTERS_MIRROR_SIZE)
    def apu = new PhysicalMemory("APU", APU_REGISTERS_START, APU_REGISTERS_END, APU_REGISTERS_SIZE)
    def apuTestMode = new PhysicalMemory("ATM", APU_TEST_REGISTERS_START, APU_TEST_REGISTERS_END, APU_TEST_REGISTERS_SIZE)
    def timer = new PhysicalMemory("TMR", TIMER_REGISTERS_START, TIMER_REGISTERS_END, TIMER_REGISTERS_SIZE)

    def newCpuBus() { new CpuBus(cycleCounter, ram, ppu, apu, apuTestMode, timer) }

    def "should read and write to ram"() {
        given:
        CpuBus bus = newCpuBus()

        def addrVal = ushort(address)
        def dataVal = ubyte(data)

        when:
        bus.specifyThen(addrVal)
        def firstSpecify = cycleCounter.diff()

        cycleCounter.mark()
        bus.writeByte(dataVal)
        def afterWrite = cycleCounter.diff()
        def controlAfterWrite = bus.currentOp()

        cycleCounter.mark()
        bus.specifyThen(addrVal)
        def secondSpecify = cycleCounter.diff()

        cycleCounter.mark()
        def read = bus.readByte()
        def afterRead = cycleCounter.diff()
        def controlAfterRead = bus.currentOp()

        then:
        sint(read) == sint(dataVal)
        firstSpecify == 1
        secondSpecify == 1
        afterWrite == 0
        afterRead == 0
        //controlAfterRead == BusOp.DATA_READ
        //controlAfterWrite == BusOp.DATA_WRITE

        where:
        address | data
        0x0000  | 0x12
        0x07FF  | 0x34
        0x0800  | 0x56
        0x0FFF  | 0x78
        0x1000  | 0x21
        0x17FF  | 0x43
        0x1800  | 0x65
        0x1FFF  | 0x87

    }

    def "should read and write to ppu"() {
        given:
        CpuBus bus = newCpuBus()

        def addrVal = ushort(address)
        def dataVal = ubyte(data)

        when:
        bus.specifyThen(addrVal).writeByte(dataVal)
        def read = bus.readByte()

        then:
        sint(read) == sint(dataVal)

        and:
        bus.specify(ushort(addrVal + 0x8))
        def read2 = bus.readByte()

        // FIXME: verify mirroring

        where:
        address | data
        0x2004  | 0xAA
    }

    // TODO: write tests for apu and cartridge
}
