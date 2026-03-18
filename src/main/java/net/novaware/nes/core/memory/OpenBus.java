package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * @see <a href="https://www.nesdev.org/wiki/Open_bus_behavior">Open Bus Behavior on nesdev.org</a>
 */
/*
 * Open bus can be limited to part of a byte. The controller ports ($4016, $4017) affect only bits 4-0.
 * Bits 7-5 repeat the corresponding bits from the previous read, usually 010 from the high byte $40.
 */
public class OpenBus implements MemoryDevice, MemoryDevice.ReadWrite  {

    private final @Unsigned short start;
    private final @Unsigned short end;

    private @Unsigned short address;
    private @Unsigned byte data;

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
        return this.onRead();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        this.onWrite(data);
    }

    public @Unsigned short lastAccess() {
        return this.address;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        this.address = address;
    }

    @Override
    public @Unsigned byte onRead() {
        return data;
    }

    @Override
    public void onWrite(@Unsigned byte data) {
        this.data = data;
    }
}
