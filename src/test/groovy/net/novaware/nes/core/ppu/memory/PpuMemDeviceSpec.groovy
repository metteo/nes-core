package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.register.*
import net.novaware.nes.core.register.BooleanRegister
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_MIRROR_END
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_START
import static net.novaware.nes.core.ppu.register.ViewPortRegister.Variant.T
import static net.novaware.nes.core.ppu.register.ViewPortRegister.Variant.VX
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PpuMemDeviceSpec extends Specification {

    def currentViewPort = new ViewPortRegister("VX", VX)
    def temporaryViewPort = new ViewPortRegister("T", T)
    def writeRegister = new BooleanRegister("W")
    def statusRegister = new PpuStatusRegister()


    def "should construct correctly"() {
        given:
        MemoryBus ppuBus = Mock()
        def ppuMemDevice = newPpuMemDev(ppuBus)

        expect:
        ppuMemDevice.getName() == "CPU<->PPU"
        ppuMemDevice.getStartAddress() == PPU_REGISTERS_START
        ppuMemDevice.getEndAddress() == PPU_REGISTERS_MIRROR_END
    }

    private PpuMemDevice newPpuMemDev(MemoryBus ppuBus) {
        new PpuMemDevice(
            ppuBus,
            currentViewPort,
            temporaryViewPort,
            writeRegister,
            statusRegister
        )
    }

    // TODO: test mirroring

    def "should redirect reads to PPU bus"() {
        given:
        PhysicalMemory ppuMem = new PhysicalMemory("PPU", PpuMemMap.MEMORY_START, PpuMemMap.MEMORY_END, PpuMemMap.MEMORY_SIZE)
        MemoryBus ppuBus = new TestBus(ppuMem)
        ppuBus.write(0x2000, 0x34)

        def ppuMemDevice = newPpuMemDev(ppuBus)
        def cpuBus = new TestBus(ppuMemDevice)

        when:
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x20))
        cpuBus.access(ushort(0x2006)).write().data(ubyte(0x00))

        def ppuData = cpuBus.access(ushort(0x2007)).read().data() // immediate read returns previous value. needs ppu cycle to get actual data

        then:
        ppuData == ubyte(0x34)
    }
}
