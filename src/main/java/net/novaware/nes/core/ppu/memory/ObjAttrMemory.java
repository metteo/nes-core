package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.util.Masks.BIT_5;
import static net.novaware.nes.core.util.Masks.BIT_6;
import static net.novaware.nes.core.util.Masks.BIT_7;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

// TODO: support both Primary and Secondary use case (eval and rendering)
// TODO: make Secondary OAM sprite limit configurable (more than 8) and also allow ghost sprites over the limit
// TODO: maybe support meta-sprite assembly / bounding box clustering (gemini: grouping nes sprites)
// TODO: consider implementing decay
/**
 * Represents OAM DRAM with both Primary and Secondary sprites
 */
public class ObjAttrMemory implements Nameable {

    // region Common OAM

    private final String name;

    // TODO: make it a multiplier: 1x 2x 4x so during ppu cycle 1 or 2 or 4 are evaluated at once?
    public ObjAttrMemory(String name, int secondarySize) {
        this.name = name;

        secondary = new ArrayList<>(secondarySize);
        for(int i = 0; i < secondarySize; i++) {
            secondary.add(new ObjAttrEntry());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" +
            Hex.s(getPrimaryStartAddress()) + ":" +
            Hex.s(getPrimaryEndAddress()) +
        ")";
    }

    // endregion
    // region Primary OAM

    public static final int PRIMARY_ENTRY_COUNT = 64;
    public static final int PRIMARY_ENTRY_SIZE  = 4; // bytes

    private final @Unsigned byte[] primary = new byte[PRIMARY_ENTRY_COUNT * PRIMARY_ENTRY_SIZE];

    public @Unsigned byte getPrimaryStartAddress() {
        return UBYTE_0;
    }

    public @Unsigned byte getPrimaryEndAddress() {
        return ubyte(PRIMARY_ENTRY_COUNT * PRIMARY_ENTRY_SIZE - 1);
    }

    public @Unsigned byte readPrimary(@Unsigned byte address) {
        int addrInt = sint(address);

        @Unsigned byte data = primary[addrInt];

        if (addrInt % 4 == 2) { // TODO: % is slow, also maybe it should be in PpuMemDevice?
            int withoutUnused = sint(data) & ~0b11100;
            return ubyte(withoutUnused);
        } else {
            return data;
        }
    }

    public void writePrimary(@Unsigned byte address, @Unsigned byte data) {
        this.primary[sint(address)] = data;
    }

    // endregion
    // region Secondary OAM

    static class ObjAttrEntry implements Cloneable { // NOTE: mutable on purpose
        @Unsigned byte y;       // byte 0
        @Unsigned byte x;       // byte 3

        @Unsigned byte tile;    // byte 1

        boolean flipH;          // byte 2
        boolean flipV;

        boolean hidden;         // behind background ("priority" in the docs)

        @Unsigned byte palette; // (4 - 7) of sprite

        @Unsigned byte unused;  // 3 bits: 2 - 4

        int primaryIndex;

        // TODO: this looks ugly, hopefully it won't be needed
        @Override protected ObjAttrEntry clone() {
            try { return (ObjAttrEntry) super.clone(); }
            catch (CloneNotSupportedException e) { throw new RuntimeException("impossibru!!!", e); }
        }
    }

    public static final int SECONDARY_ENTRY_COUNT = 8;

    private final List<ObjAttrEntry> secondary;

    public int getSecondarySize() {
        return secondary.size();
    }

    /**
     *
     * @param index of the sprite (0-63 for primary, 0-8 for secondary)
     * @return 4 bytes in form of int (y, tile, attr, x)
     */
    public ObjAttrEntry getSecondary(int index) {
        // TODO: temporary cloning
        return secondary.get(index).clone();
    }

    // endregion
    // Evaluation

    void copyToSecondary(int primaryIndex, int secondaryIndex) {
        assert 0 <= primaryIndex && primaryIndex < PRIMARY_ENTRY_COUNT : "primaryIndex out of bounds";
        assert 0 <= secondaryIndex && secondaryIndex < secondary.size() : "secondaryIndex out of bounds";

        ObjAttrEntry entry = secondary.get(secondaryIndex);

        decodeBytesIntoEntry(primaryIndex, entry);
    }

    void decodeBytesIntoEntry(int primaryIndex, ObjAttrEntry entry) {
        int baseAddress = primaryIndex * PRIMARY_ENTRY_SIZE;

        entry.y = primary[baseAddress];
        entry.tile = primary[baseAddress + 1];

        int byte2 = sint(primary[baseAddress + 2]);
        decodeByte2(byte2, entry);

        entry.x = primary[baseAddress + 3];

        entry.primaryIndex = primaryIndex;
    }

    static void decodeByte2(int byte2, ObjAttrEntry entry) {
        entry.flipV = (byte2 & BIT_7) != 0;
        entry.flipH = (byte2 & BIT_6) != 0;
        entry.hidden = (byte2 & BIT_5) != 0;
        entry.unused = ubyte(byte2 >> 2 & 0b111);
        entry.palette = ubyte(byte2 & 0b11);
    }

    // TODO: evaluation happens over several ppu cycles, not instantly?
    public void evaluate(int scanline, ObjAttrMemory target) {
        throw new UnsupportedOperationException("not implemented!");
    }

    public String printOam() {
        StringBuilder sb = new StringBuilder();
        sb.append("Primary:\n");

        for (int i = 0; i < PRIMARY_ENTRY_COUNT; i++) {
            ObjAttrEntry entry = new ObjAttrEntry();
            decodeBytesIntoEntry(i, entry);

            sb.append("\t").append(i).append(" -> ").append(printEntry(entry)).append("\n");
        }

        sb.append("Secondary:\n");

        for (int i = 0; i < secondary.size(); i++) {
            ObjAttrEntry entry = secondary.get(i);
            sb.append("\t").append(i).append(" -> ").append(entry.primaryIndex).append(" -> ")
                    .append(printEntry(entry)).append("\n");
        }

        return sb.toString();
    }

    static String printEntry(ObjAttrEntry entry) {
        StringBuilder sb = new StringBuilder();

        sb.append("y: 0x").append(Hex.s(entry.y)).append(", ");
        sb.append("x: 0x").append(Hex.s(entry.x)).append(", ");
        sb.append("tile: 0x").append(Hex.s(entry.tile)).append(", ");
        sb.append("palette: ").append(sint(entry.palette)).append(", ");
        sb.append("flipV: ").append(entry.flipV).append(", ");
        sb.append("flipH: ").append(entry.flipH).append(", ");
        sb.append("hidden: ").append(entry.hidden).append(", ");
        sb.append("unused: ").append(sint(entry.unused));

        return sb.toString();
    }

    // endregion
}
