package net.novaware.nes.core.ppu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.util.Bin;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * EXT0..3 pins for Master / Slave PPU comms
 */
@BoardScope
public class ExtBus {

    private static final int PIN_MASK = 0b1111;

    private @Unsigned byte pins;

    @Inject
    public ExtBus() {}

    public @Unsigned byte read() {
        return pins;
    }

    public void write(@Unsigned byte data) {
        this.pins = ubyte(sint(data) & PIN_MASK);
    }

    @Override
    public String toString() {
        return "EXT " + Bin.s(sint(pins), 4);
    }
}
