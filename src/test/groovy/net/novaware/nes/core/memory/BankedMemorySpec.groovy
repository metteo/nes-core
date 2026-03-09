package net.novaware.nes.core.memory

import net.novaware.nes.core.util.Quantity
import spock.lang.Specification

import static net.novaware.nes.core.util.Quantity.Unit.*
import static net.novaware.nes.core.util.UTypes.ubyte
import static net.novaware.nes.core.util.UTypes.ushort

// TODO: make it more clever, eg fill whole hidden space with unique values and then assert edges and random mids
class BankedMemorySpec extends Specification {

    def "should map 64KB of data over 32KB address space (switching)"() {
        given:
        def memory = new BankedMemory(
            ushort(0x8000),
            new Quantity(2, BANK_16KB),
            new Quantity(4, BANK_16KB)
        )
    }

    def "should map 32KB of data over 32KB address space (direct)"() {
        given:
        def memory = new BankedMemory(
                ushort(0x8000),
                new Quantity(2, BANK_16KB),
                new Quantity(2, BANK_16KB)
        )

        memory.configure(0, 0)
        memory.configure(1, 1)

        when:
        memory.specifyThen(ushort(0x8000)).writeByte(ubyte(0x11))
        memory.specifyThen(ushort(0x9000)).writeByte(ubyte(0x22))
        memory.specifyThen(ushort(0xA000)).writeByte(ubyte(0x33))
        memory.specifyThen(ushort(0xB000)).writeByte(ubyte(0x44))
        memory.specifyThen(ushort(0xBFFF)).writeByte(ubyte(0x55))

        memory.specifyThen(ushort(0xC000)).writeByte(ubyte(0x66))
        memory.specifyThen(ushort(0xD000)).writeByte(ubyte(0x77))
        memory.specifyThen(ushort(0xE000)).writeByte(ubyte(0x88))
        memory.specifyThen(ushort(0xF000)).writeByte(ubyte(0x99))
        memory.specifyThen(ushort(0xFFFF)).writeByte(ubyte(0xAA))

        then:
        memory.specifyThen(ushort(0x8000)).readByte() == ubyte(0x11)
        memory.specifyThen(ushort(0x9000)).readByte() == ubyte(0x22)
        memory.specifyThen(ushort(0xA000)).readByte() == ubyte(0x33)
        memory.specifyThen(ushort(0xB000)).readByte() == ubyte(0x44)
        memory.specifyThen(ushort(0xBFFF)).readByte() == ubyte(0x55)

        memory.specifyThen(ushort(0xC000)).readByte() == ubyte(0x66)
        memory.specifyThen(ushort(0xD000)).readByte() == ubyte(0x77)
        memory.specifyThen(ushort(0xE000)).readByte() == ubyte(0x88)
        memory.specifyThen(ushort(0xF000)).readByte() == ubyte(0x99)
        memory.specifyThen(ushort(0xFFFF)).readByte() == ubyte(0xAA)
    }

    def "should mirror 16KB of data over 32KB address space (mirroring)"() {
        given:
        def memory = new BankedMemory(
                ushort(0x8000),
                new Quantity(2, BANK_16KB),
                new Quantity(1, BANK_16KB)
        )

        memory.configure(0, 0)
        memory.configure(1, 0)

        when:
        memory.specifyThen(ushort(0x8000)).writeByte(ubyte(0x11))
        memory.specifyThen(ushort(0x9000)).writeByte(ubyte(0x22))
        memory.specifyThen(ushort(0xA000)).writeByte(ubyte(0x33))
        memory.specifyThen(ushort(0xB000)).writeByte(ubyte(0x44))
        memory.specifyThen(ushort(0xBFFF)).writeByte(ubyte(0x55))

        then:
        memory.specifyThen(ushort(0x8000)).readByte() == ubyte(0x11)
        memory.specifyThen(ushort(0x9000)).readByte() == ubyte(0x22)
        memory.specifyThen(ushort(0xA000)).readByte() == ubyte(0x33)
        memory.specifyThen(ushort(0xB000)).readByte() == ubyte(0x44)
        memory.specifyThen(ushort(0xBFFF)).readByte() == ubyte(0x55)

        memory.specifyThen(ushort(0xC000)).readByte() == ubyte(0x11)
        memory.specifyThen(ushort(0xD000)).readByte() == ubyte(0x22)
        memory.specifyThen(ushort(0xE000)).readByte() == ubyte(0x33)
        memory.specifyThen(ushort(0xF000)).readByte() == ubyte(0x44)
        memory.specifyThen(ushort(0xFFFF)).readByte() == ubyte(0x55)
    }
}
