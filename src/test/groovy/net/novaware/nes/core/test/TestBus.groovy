package net.novaware.nes.core.test

import groovy.util.logging.Log
import net.novaware.nes.core.memory.*
import org.checkerframework.checker.signedness.qual.Unsigned

import static net.novaware.nes.core.util.UTypes.*

/**
 * Simple bus for wrapping a single MemoryDevice.
 * Makes calling it easier (handles the DataLine)
 *
 * Doesn't support attach / detach on purpose
 */
@Log
class TestBus implements MemoryBus {

    MemoryDevice.ReadWrite device

    int addressLatch;
    DataLine dataLine = new DataLine()

    TestBus(MemoryDevice.ReadWrite device) {
        this.device = device

        // Useful when same device gets attached for init and then attached again for testing
        log.info("Attaching device: " + device + " to TestBus")
        device.onAttach(dataLine)
    }

    @Override
    byte peek(@Unsigned short address) {
        return read(address)
    }

    int read(int address) {
        device.onAccess(ushort(address))
        device.onRead()

        def read = dataLine.cycle()
        return sint(read)
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
