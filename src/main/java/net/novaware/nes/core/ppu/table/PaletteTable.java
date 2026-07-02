package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.memory.PaletteMemory;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertNonNull;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class PaletteTable implements Table {

    private final String name;
    private final PaletteMemory memory;

    public PaletteTable(String name, PaletteMemory memory) {
        this.name = name;
        this.memory = memory;
    }

    /* package */ static @Unsigned byte getAddress(Layer layer, int palette, int offset) {
        assert 0 <= palette && palette < 4 : "palette not in range";
        assert 0 <= offset && offset < 4 : "offset not in range";

        int address = layer.bit | (palette << 2) | offset;

        return ubyte(address);
    }

    public @Unsigned byte getColorRef(Layer layer, int palette, int offset) {
        assertNonNull(layer, "layer must not be null");

        @Unsigned byte address = getAddress(layer, palette, offset);

        return memory.read(address);
    }

    /* package */ void setColorRef(Layer layer, int palette, int offset, @Unsigned byte colorRef) {
        assertNonNull(layer, "layer must not be null");

        @Unsigned byte address = getAddress(layer, palette, offset);

        memory.write(address, colorRef);
    }

    @Override
    public String getName() {
        return name;
    }

    public enum Layer {
        BACKGROUND(0x00), // 0
        FOREGROUND(0x10), // 1 - sprite
        ;
        private final int bit;

        Layer(int bit) {
            this.bit = bit;
        }

        public int bit() {
            return bit;
        }
    }
}
