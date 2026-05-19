package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.util.UTypes.sint;

// TODO: support both Primary and Secondary use case (eval and rendering)
// TODO: make Secondary OAM sprite limit configurable (more than 8) and also allow ghost sprites over the limit
// TODO: maybe support meta-sprite assembly / bounding box clustering (gemini: grouping nes sprites)
public class ObjAttrMemory implements Nameable { // TODO: implement in a performant way for PPU. CPU / DMA can pay for conversion

    public static final int ENTRY_COUNT = 64;
    public static final int ENTRY_SIZE = 4;

    static class ObjAttrEntry { // NOTE: mutable on purpose
        @Unsigned byte y;       // byte 0
        @Unsigned byte x;       // byte 3

        @Unsigned short bank;   // byte 1
        @Unsigned byte tile;

        boolean horizontalFlip; // byte 2
        boolean verticalFlip;

        boolean hidden; // behind background ("priority" in the docs)

        @Unsigned byte palette; // (4 - 7) of sprite

        @Unsigned byte unused; // 3 bits: 2 - 4
    }

    private final String name;

    // TODO: create an index per scanline (y) to quickly fetch entries instead of iterating all of them
    // Rebuild the index after DMA writes instead of after every byte
    private final List<ObjAttrEntry> entries = new ArrayList<>(ENTRY_COUNT);

    private final @Unsigned byte[] data = new byte[0x100];

    public ObjAttrMemory(String name) {
        this.name = name;

        for(int i = 0; i < ENTRY_COUNT; i++) {
            entries.add(new ObjAttrEntry());
        }
    }

    /**
     *
     * @param index of the sprite (0-63)
     * @return 4 bytes in form of int (y, tile, attr, x)
     */
    public int getObject(int index) {
        throw new UnsupportedOperationException("not implemented!");
    }

    /**
     *
     * @param index of the sprite (0-63)
     * @param data 4 bytes in form of int (y, tile, attr, x)
     */
    public void putObject(int index, int data) {
        throw new UnsupportedOperationException("not implemented!");
    }

    public @Unsigned byte read(@Unsigned byte address) {
        return data[sint(address)];
    }

    public void write(@Unsigned byte address, @Unsigned byte data) {
        this.data[sint(address)] = data;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (00:FF)";
    }
}
