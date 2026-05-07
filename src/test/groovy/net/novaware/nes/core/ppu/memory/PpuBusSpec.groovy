package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.memory.RecordingDevice
import net.novaware.nes.core.register.CycleCounter
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.memory.BusOp.ADDRESS_ACCESS
import static net.novaware.nes.core.memory.BusOp.CONTROL_WRITE
import static net.novaware.nes.core.memory.BusOp.DATA_READ
import static net.novaware.nes.core.memory.BusOp.DATA_WRITE
import static net.novaware.nes.core.memory.RecordingDevice.*
import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

class PpuBusSpec extends Specification {

    def "should read and write to ppu bus"() {
        given:
        PhysicalMemory cartridge = new PhysicalMemory(
                "CART", MEMORY_START, UNUSED_END,
                MEMORY_SIZE - PALETTE_RAM_MIRROR_SIZE
        )

        def rec = new RecordingDevice(new CycleCounter("PPU")) // ppu bus doesn't count cycles

        // put some data not using the test subject
        def cartBus = new TestBus(cartridge)
        cartBus.access(ushort(0x0000)).write().data(ubyte(0x12))
        cartBus.access(ushort(0x3EFF)).write().data(ubyte(0x34))
        cartridge.onDetach()

        PpuBus bus = new PpuBus()
        bus.attachCartridge(cartridge)
        bus.attachExpansion(rec)

        when:
        rec.record()
        bus.access(ushort(0x0001)).write().data(ubyte(0x56))
        def maybeWrite = bus.currentOp()
        def activity = rec.activity()

        bus.access(ushort(0x3EFE)).write().data(ubyte(0x78))

        then: "cartridge works"
        bus.access(ushort(0x0000)).read().data() == ubyte(0x12)
        bus.access(ushort(0x3EFF)).read().data() == ubyte(0x34)
        bus.access(ushort(0x0001)).read().data() == ubyte(0x56)
        bus.access(ushort(0x3EFE)).read().data() == ubyte(0x78)

        // currentOp works
        maybeWrite == DATA_WRITE
        bus.currentOp() == DATA_READ

        // expansion works
        rec.activity().size() == 18

        activity == [
            new Op(ADDRESS_ACCESS, 0x0001, 0x00),
            new Op(CONTROL_WRITE,  0x0001, 0x00),
            new Op(DATA_WRITE,     0x0001, 0x56),
        ]

        and:
        bus.detachCartridge()
        bus.detachExpansion()

        then: "detaching works"
        rec.record()

        bus.access(ushort(0x0000)).read().data() == ubyte(0x78) // open bus, prev value
        bus.access(ushort(0x0000)).read().data() == ubyte(0xFF) // open bus, no value

        rec.activity().size() == 0
    }
}
