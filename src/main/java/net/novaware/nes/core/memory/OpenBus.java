package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * @see <a href="https://www.nesdev.org/wiki/Open_bus_behavior">Open Bus Behavior on nesdev.org</a>
 */
/*
 * Open bus can be limited to part of a byte. The controller ports ($4016, $4017) affect only bits 4-0.
 * Bits 7-5 repeat the corresponding bits from the previous read, usually 010 from the high byte $40.
 */
public class OpenBus implements MemoryDevice, MemoryDevice.ReadWrite  { // TODO: probably remove. TempLine is taking over the openbus simulation, probably

    private final @Unsigned short start;
    private final @Unsigned short end;

    private @Unsigned short address;
    private @Unsigned byte data;

    private DataBus.Line dataLine = new OpenCircuit();

    public OpenBus(@Unsigned short start, @Unsigned short end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return start;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return end;
    }

    @Override
    public void specify(@Unsigned short address) {
        this.onAccess(address);
    }

    @Override
    public @Unsigned byte readByte() {
        TempLine tl = new TempLine();
        this.onRead();

        return tl.data();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        this.onWrite();
    }

    public @Unsigned short lastAddress() {
        return address;
    }

    public @Unsigned byte lastData() {
        return data;
    }

    public void lastData(@Unsigned byte data) { // separate from API for mocking
        this.data = data;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        this.address = address;
    }

    @Override
    public void onRead() {
        if (dataLine instanceof TempLine tempLine) {
            tempLine.data(tempLine.isOpenBus() ? data : tempLine.data());
        } else {
            throw new IllegalArgumentException("no way to check if anything is on the bus");
        }
    }

    @Override
    public void onWrite() {
        this.data = dataLine.data();
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenCircuit();
    }
}
