package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * @see <a href="https://www.nesdev.org/wiki/Open_bus_behavior">Open Bus Behavior on nesdev.org</a>
 */
public class OpenBus implements MemoryDevice { // TODO: consider making it more complete

    private final @Unsigned short start;
    private final @Unsigned short end;

    private @Unsigned short address;

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
        this.address = address;
    }

    @Override
    public @Unsigned byte readByte() {
        return ubyte((sint(address) & 0xFF00) >> 8);
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        // NOOP
    }
}
