package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.List;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.novaware.nes.core.ppu.unit.PaletteData.COLOR_TRANSPARENT;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * @see net.novaware.nes.core.ppu.unit.PaletteData for actual colors in RGB
 */
public class PaletteMemory implements Nameable {

    public static final int SIZE = 0x20;
    public static final int MASK = SIZE - 1;

    private final String name;

    private final UByteBuffer buffer;

    public PaletteMemory(String name) {
        this.name = name;

        final @Unsigned byte black = ubyte(0x0F);

        buffer = UByteBuffer.allocate(SIZE)
                .order(LITTLE_ENDIAN)
                .fill(black);

        // Poison mirrored, unreachable slots
        List.of(0x10, 0x14, 0x18, 0x1C)
                .forEach(i -> buffer.put(i, COLOR_TRANSPARENT));
    }

    @Override
    public String getName() {
        return name;
    }

    /* package */ int toPosition(@Unsigned byte address) {
        int position = sint(address) & MASK;

        int layer = position & 0x10;
        int palette = position & 0b1100;
        int offset  =  position & 0b0011;

        int adjLayer = offset == 0 ? 0 : layer;

        int adjPosition = adjLayer | palette | offset;

        return adjPosition;
    }

    public @Unsigned byte read(@Unsigned byte address) {
        int position = toPosition(address);
        @Unsigned byte color = buffer.get(position);
        return color;
    }

    public void write(@Unsigned byte address, @Unsigned byte colorRef) {
        int position = toPosition(address);
        buffer.put(position, colorRef);
    }

    @Override
    public String toString() {
        return name + " (" + Hex.s(0) + ":" + Hex.s(MASK) + ")";
    }
}
