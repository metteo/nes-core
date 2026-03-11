package net.novaware.nes.core.cpu.memory

import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.register.CycleCounter
import spock.lang.Specification

import static net.novaware.nes.core.util.UTypes.*

class CpuBusSpec extends Specification {

    def cycleCounter = new CycleCounter("CPUCC")

    def newCpuBus() {
        new CpuBus(
            cycleCounter,
            new PhysicalMemory(CpuMemMap.RAM_SIZE, sint(CpuMemMap.RAM_START)),
            new PhysicalMemory(CpuMemMap.PPU_REGISTERS_MIRROR_SIZE, sint(CpuMemMap.PPU_REGISTERS_START)),
            new PhysicalMemory(CpuMemMap.APU_REGISTERS_SIZE, sint(CpuMemMap.APU_REGISTERS_START)),
            null,
            null,
            null,
            null
        )
    }

    def "should read and write to ram"() {
        given:
        CpuBus bus = newCpuBus()
        def address = ushort(0x0002)
        def data = ubyte(0x3)

        when:
        bus.specifyThen(address).writeByte(data)
        def read = bus.readByte()

        then:
        sint(read) == sint(data)

        and:
        bus.specifyThen(ushort(0x4)).writeByte(ubyte(0x5))

        then:
        bus.specify(address)
        sint(bus.readByte()) == sint(data)

        // TODO: verify mirroring works
    }

    def "should read and write to ppu register"() {
        given:
        CpuBus bus = newCpuBus()
        def address = ushort(0x2004)
        def data = ubyte(0xAA)

        when:
        bus.specifyThen(address).writeByte(data)
        def read = bus.readByte()

        then:
        sint(read) == sint(data)
        PhysicalMemory ppu = bus.ppuRegs
        ppu.buffer.getAsInt(4) == sint(data)

        and:
        bus.specify(ushort(0x2004 + 0x8))
        def read2 = bus.readByte()

        //then:
        // sint(read2) == sint(data) // FIXME: verify mirroring
    }

    // TODO: write tests for apu and cartridge
}
