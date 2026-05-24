package net.novaware.nes.core.easy

import net.novaware.nes.core.easy.inject.EasyMemModule
import net.novaware.nes.core.easy.memory.EasyBus
import net.novaware.nes.core.memory.MemoryDevice
import net.novaware.nes.core.memory.PagedMemory
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.register.IntegerCounter
import net.novaware.nes.core.register.SegmentRegister
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.easy.memory.EasyMemMap.*
import static net.novaware.nes.core.util.ProbeUtil.probeBus
import static net.novaware.nes.core.util.UTypes.ubyte

class EasyBusSpec extends Specification {

    IntegerCounter cc = new IntegerCounter("CPU.CC")
    MemoryDevice.ReadWrite ram = EasyMemModule.provideMemory()
    MemoryDevice.ReadWrite stack = EasyMemModule.provideStack()
    MemoryDevice.ReadWrite vram = EasyMemModule.provideVideoMemory()

    PagedMemory cartridgeRoot = new PagedMemory("CARTRIDGE", MEMORY_SIZE, new MemoryDevice.Empty())
    MemoryDevice.ReadWrite cartridge = new PhysicalMemory("CART", CARTRIDGE_START, CARTRIDGE_END, CARTRIDGE_SIZE)
    def codeSegment = new SegmentRegister("CS")

    EasyBus bus = new EasyBus(cc, ram, stack, vram, codeSegment)

    def "should write to correct memory segments"() {
        given:
        cartridgeRoot.attach(cartridge)
        bus.attachCartridge(cartridgeRoot)

        def devices = [
            "ram":   ram,
            "stack": stack,
            "vram":  vram,
            "cart":  cartridge
        ]

        when:
        bus.access(address).write().data(ubyte(data))

        then:
        bus.access(address).read().data() == ubyte(data)
        probeBus(bus, address) == ubyte(data)
        new TestBus(devices[device]).access(address).read().data() == ubyte(data)

        where:
        device  | address               | data
        "ram"   | RAM_START             | 0x11
        "ram"   | RAM_END               | 0x22
        "stack" | STACK_SEGMENT_START   | 0x33
        "stack" | STACK_SEGMENT_END     | 0x44
        "vram"  | PICTURE_SEGMENT_START | 0x55
        "vram"  | PICTURE_SEGMENT_END   | 0x66
        "cart"  | CARTRIDGE_START       | 0x77
        "cart"  | CARTRIDGE_END         | 0x88
    }
}
