package net.novaware.nes.core.test

import net.novaware.nes.core.memory.*
import org.checkerframework.checker.signedness.qual.Unsigned

import static net.novaware.nes.core.util.UTypes.*

/**
 * Simple bus for wrapping a single MemoryDevice.
 * Makes calling it easier (handles the DataLine)
 *
 * Doesn't support attach / detach on purpose
 */
class TestBus implements MemoryBus {

    MemoryDevice.ReadWrite device

    int addressLatch;
    DataLine dataLine = new DataLine()

    TestBus(MemoryDevice.ReadWrite device) {
        this.device = device

        device.onAttach(dataLine)
    }

    int read(int address) {
        device.onAccess(ushort(address))
        device.onRead()

        return sint(dataLine.cycle())
    }

    void write(int address, int data) {
        device.onAccess(ushort(address))

        dataLine.data(ubyte(data))
        device.onWrite()
        dataLine.cycle()
    }

    @Override
    ControlBus.Line access(@Unsigned short address) {
        addressLatch = sint(address)

        return this
    }

    @Override
    DataBus.Read read() {
        return this
    }

    @Override
    DataBus.Write write() {
        return this
    }

    @Override
    byte data() {
        return ubyte(read(addressLatch))
    }

    @Override
    void data(@Unsigned byte data) {
        write(addressLatch, sint(data))
    }

    @Override
    BusOp currentOp() {
        throw new UnsupportedOperationException("not implemented")
    }

    @Override
    void attachCartridge(MemoryDevice.ReadWrite cartridge) {
        throw new UnsupportedOperationException("not implemented")
    }

    @Override
    void detachCartridge() {
        throw new UnsupportedOperationException("not implemented")
    }

    @Override
    void attachExpansion(MemoryDevice.ReadWrite expansion) {
        throw new UnsupportedOperationException("not implemented")
    }

    @Override
    void detachExpansion() {
        throw new UnsupportedOperationException("not implemented")
    }
}
