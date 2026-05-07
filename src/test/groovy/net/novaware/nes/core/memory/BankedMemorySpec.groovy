package net.novaware.nes.core.memory

import net.novaware.nes.core.test.TestBus
import net.novaware.nes.core.util.Quantity
import spock.lang.Specification

import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

// TODO: make it more clever, eg fill whole hidden space with unique values and then assert edges and random mids
class BankedMemorySpec extends Specification {

    def "should map 64KB of data over 32KB address space (switching)"() {
        given:
        def bankedMemory = new BankedMemory(
            "NN",
            ushort(0x8000),
            new Quantity(1, BANK_16KB)
        )
        bankedMemory
            .setVirtualBanks(new Quantity(2, BANK_16KB))
            .setPhysicalBanks(new Quantity(4, BANK_16KB))

        expect:
        bankedMemory.getEndAddress() == ushort(0xFFFF)
        bankedMemory.getPhysicalBankCount() == new Quantity(4, BANK_16KB)
        bankedMemory.getVirtualBankCount() == new Quantity(2, BANK_16KB)
    }

    def "should map 32KB of data over 32KB address space (direct)"() {
        given:
        def bankedMemory = new BankedMemory(
            "NN",
            ushort(0x8000),
            new Quantity(1, BANK_16KB)
        )
        bankedMemory
            .setPhysicalBanks(new Quantity(2, BANK_16KB))
            .allocatePhysicalBanks()

        bankedMemory
            .setVirtualBanks(new Quantity(2, BANK_16KB))
            .mapVirtualToPhysical(0, 0)
            .mapVirtualToPhysical(1, 1)

        def memory = new TestBus(bankedMemory)

        when:
        memory.access(ushort(0x8000)).write().data(ubyte(0x11))
        memory.access(ushort(0x9000)).write().data(ubyte(0x22))
        memory.access(ushort(0xA000)).write().data(ubyte(0x33))
        memory.access(ushort(0xB000)).write().data(ubyte(0x44))
        memory.access(ushort(0xBFFF)).write().data(ubyte(0x55))

        memory.access(ushort(0xC000)).write().data(ubyte(0x66))
        memory.access(ushort(0xD000)).write().data(ubyte(0x77))
        memory.access(ushort(0xE000)).write().data(ubyte(0x88))
        memory.access(ushort(0xF000)).write().data(ubyte(0x99))
        memory.access(ushort(0xFFFF)).write().data(ubyte(0xAA))

        then:
        bankedMemory.getStartAddress() == ushort(0x8000)
        bankedMemory.getEndAddress() == ushort(0xFFFF)

        memory.access(ushort(0x8000)).read().data() == ubyte(0x11)
        memory.access(ushort(0x9000)).read().data() == ubyte(0x22)
        memory.access(ushort(0xA000)).read().data() == ubyte(0x33)
        memory.access(ushort(0xB000)).read().data() == ubyte(0x44)
        memory.access(ushort(0xBFFF)).read().data() == ubyte(0x55)

        memory.access(ushort(0xC000)).read().data() == ubyte(0x66)
        memory.access(ushort(0xD000)).read().data() == ubyte(0x77)
        memory.access(ushort(0xE000)).read().data() == ubyte(0x88)
        memory.access(ushort(0xF000)).read().data() == ubyte(0x99)
        memory.access(ushort(0xFFFF)).read().data() == ubyte(0xAA)
    }

    def "should mirror 16KB of data over 32KB address space (mirroring)"() {
        given:
        def bankedMemory = new BankedMemory(
            "NN",
            ushort(0x8000),
            new Quantity(1, BANK_16KB)
        )
        bankedMemory
            .setPhysicalBanks(new Quantity(1, BANK_16KB))
            .allocatePhysicalBanks()

        bankedMemory
            .setVirtualBanks(new Quantity(2, BANK_16KB))
            .mapVirtualToPhysical(0, 0)
            .mapVirtualToPhysical(1, 0)

        def memory = new TestBus(bankedMemory)

        when:
        memory.access(ushort(0x8000)).write().data(ubyte(0x11))
        memory.access(ushort(0x9000)).write().data(ubyte(0x22))
        memory.access(ushort(0xA000)).write().data(ubyte(0x33))
        memory.access(ushort(0xB000)).write().data(ubyte(0x44))
        memory.access(ushort(0xBFFF)).write().data(ubyte(0x55))

        then:
        bankedMemory.getStartAddress() == ushort(0x8000)
        bankedMemory.getEndAddress() == ushort(0xFFFF)

        memory.access(ushort(0x8000)).read().data() == ubyte(0x11)
        memory.access(ushort(0x9000)).read().data() == ubyte(0x22)
        memory.access(ushort(0xA000)).read().data() == ubyte(0x33)
        memory.access(ushort(0xB000)).read().data() == ubyte(0x44)
        memory.access(ushort(0xBFFF)).read().data() == ubyte(0x55)

        memory.access(ushort(0xC000)).read().data() == ubyte(0x11)
        memory.access(ushort(0xD000)).read().data() == ubyte(0x22)
        memory.access(ushort(0xE000)).read().data() == ubyte(0x33)
        memory.access(ushort(0xF000)).read().data() == ubyte(0x44)
        memory.access(ushort(0xFFFF)).read().data() == ubyte(0x55)
    }
}
