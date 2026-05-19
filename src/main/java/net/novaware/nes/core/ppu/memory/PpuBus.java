package net.novaware.nes.core.ppu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.memory.BusOp;
import net.novaware.nes.core.memory.ControlBus;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.DataLine;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

@SuppressWarnings({"initialization.fields.uninitialized", "return"}) // TODO: remove when fully implemented
public class PpuBus implements MemoryBus {

    // TODO: cart and expansion don't have the pallete indexes and don't hear anything above 0x3F00 (excl.)
    @Used
    private MemoryDevice.ReadWrite cartridge = new MemoryDevice.Empty(); // TODO: consider using single injected instance to allow testing

    @Used
    private MemoryDevice.ReadWrite expansion = new MemoryDevice.Empty();

    private final DataLine dataLine = new DataLine();

    private BusOp busOp = BusOp.DATA_READ; // TODO: randomize between data read / write

    private @Unsigned short addressLatch;

    @Inject
    public PpuBus() {

    }

    @Override
    public BusOp currentOp() {
        return busOp;
    }

    @Override
    public void attachCartridge(MemoryDevice.ReadWrite cartridge) {
        this.cartridge = cartridge;
        this.cartridge.onAttach(dataLine);
    }

    @Override
    public void detachCartridge() {
        cartridge.onDetach();
        cartridge = new MemoryDevice.Empty();
    }

    @Override
    public void attachExpansion(MemoryDevice.ReadWrite expansion) {
        this.expansion = expansion;
        this.expansion.onAttach(dataLine);
    }

    @Override
    public void detachExpansion() {
        expansion.onDetach();
        expansion = new MemoryDevice.Empty();
    }

    @Override
    public ControlBus.Line access(@Unsigned short address) {
        assert busOp == BusOp.DATA_READ || busOp == BusOp.DATA_WRITE; // compile out, TODO: consider JCP or Manifold

        busOp = BusOp.ADDRESS_ACCESS;
        addressLatch = address;

        // PPU AD0..7 are shared between address and data bus
        // dataLine.data(ubyte(sint(addressLatch) & 0xFF)); // TODO: uncomment and update tests

        cartridge.onAccess(addressLatch);
        expansion.onAccess(addressLatch);

        return this;
    }

    @Override
    public DataBus.Read read() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out

        busOp = BusOp.CONTROL_READ;

        return this;
    }

    @Override
    public DataBus.Write write() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out

        busOp = BusOp.CONTROL_WRITE;

        return this;
    }

    @Override // TODO: verify that the bus is reverted to correct state
    public @Unsigned byte peek(@Unsigned short address) {
        return 0; // TODO: implement
    }

    @Override
    public @Unsigned byte data() {
        assert busOp == BusOp.CONTROL_READ; // compile out

        busOp = BusOp.DATA_READ;

        cartridge.onRead();
        expansion.onRead();

        return dataLine.cycle(); // PPU reading the line
    }

    @Override
    public void data(@Unsigned byte data) {
        assert busOp == BusOp.CONTROL_WRITE; // compile out

        busOp = BusOp.DATA_WRITE;

        dataLine.data(data); // PPU driving the line

        cartridge.onWrite();
        expansion.onWrite();

        dataLine.cycle();
    }
}
