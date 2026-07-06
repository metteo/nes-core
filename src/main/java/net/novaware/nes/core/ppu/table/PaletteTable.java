package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.table.Palette.Layer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertNonNull;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class PaletteTable implements Table {

    public static final int ROW_COUNT = 4;
    public static final int COL_COUNT = 4;

    private final String name;
    private final PaletteMemory memory;

    public PaletteTable(String name, PaletteMemory memory) {
        this.name = name;
        this.memory = memory;
    }

    @Override
    public String getName() {
        return name;
    }

    /* package */ static @Unsigned byte getAddress(Layer layer, int row, int col) {
        assert 0 <= row && row < ROW_COUNT : "row not in range";
        assert 0 <= col && col < COL_COUNT : "col not in range";

        // NOTE: PaletteMemory handles sharing col=0 between layers
        int address = layer.bit() | (row << 2) | col;

        return ubyte(address);
    }

    public @Unsigned byte getColorRef(Layer layer, int row, int col) {
        assert layer != null : "layer must not be null";

        @Unsigned byte address = getAddress(layer, row, col);

        return memory.read(address);
    }

    /* package */ void setColorRef(Layer layer, int row, int col, @Unsigned byte colorRef) {
        assertNonNull(layer, "layer must not be null");

        @Unsigned byte address = getAddress(layer, row, col);

        memory.write(address, colorRef);
    }

    @Override
    public String toString() {
        return name + " (" + Layer.values().length + ":" + ROW_COUNT + ":" + COL_COUNT + ")";
    }
}
