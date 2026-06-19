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

    // TODO: make it a multiplier: 1x or 2x so during ppu cycle unused nt fetches can be used for sprite fetch
    public ObjAttrMemory(String name, int secondarySize) {
        this.name = name;

        secondary = new ArrayList<>(secondarySize);
        for(int i = 0; i < secondarySize; i++) {
            secondary.add(new ObjAttrEntry());
        }
        secondaryAddressMask = secondarySize * ENTRY_SIZE - 1;
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
    public static final int ENTRY_SIZE = 4; // bytes

    private final @Unsigned byte[] primary = new byte[PRIMARY_ENTRY_COUNT * ENTRY_SIZE];

    public @Unsigned byte getPrimaryStartAddress() {
        return UBYTE_0;
    }

    public @Unsigned byte getPrimaryEndAddress() {
        return ubyte(PRIMARY_ENTRY_COUNT * ENTRY_SIZE - 1);
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

        @Unsigned byte tile;    // byte 1

        //                      // byte 2 v
        boolean flipH;          //  bit 7
        boolean flipV;          //  bit 6

        boolean hidden;         //  bit 5 - behind background ("priority" in the docs)

        @Unsigned byte unused;  //  bit 2-4

        @Unsigned byte palette; //  bit 0-1

        @Unsigned byte x;       // byte 3

        int primaryIndex = -1;
    }

    public static final int SECONDARY_ENTRY_COUNT = 8;

    private final int secondaryAddressMask;
    private final List<ObjAttrEntry> secondary;

    public int getSecondarySize() {
        return secondary.size();
    }

    public ObjAttrEntry getSecondary(int index) {
        return secondary.get(index);
    }

    public void writeSecondary(@Unsigned byte address, @Unsigned byte data) {
        int addrInt = sint(address) & secondaryAddressMask;

        int index = addrInt / 4; // TODO: slow
        int byteNum = addrInt % 4; // TODO: slow

        ObjAttrEntry entry = secondary.get(index);

        switch (byteNum) {
            case 0:
                entry.y = data;
                break;
            case 1:
                entry.tile = data;
                entry.primaryIndex = -1;
                break;
            case 2:
                decodeByte2(sint(data), entry);
                break;
            case 3:
                entry.x = data;
                break;
            default:
                throw new IllegalStateException("Unexpected byte number within entry: " + byteNum);
        }
    }

    // endregion
    // Evaluation

    public void copyToSecondary(int primaryIndex, int secondaryIndex) {
        assert 0 <= primaryIndex && primaryIndex < PRIMARY_ENTRY_COUNT : "primaryIndex out of bounds";
        assert 0 <= secondaryIndex && secondaryIndex < secondary.size() : "secondaryIndex out of bounds";

        ObjAttrEntry entry = secondary.get(secondaryIndex);

        decodeBytesIntoEntry(primaryIndex, entry);
    }

    void decodeBytesIntoEntry(int primaryIndex, ObjAttrEntry entry) {
        int baseAddress = primaryIndex * ENTRY_SIZE;

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
