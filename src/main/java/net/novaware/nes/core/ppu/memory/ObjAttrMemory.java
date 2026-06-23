package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Nums.powOfTwo;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

// TODO: make Secondary OAM sprite limit configurable (more than 8) and also allow ghost sprites over the limit
// TODO: maybe support meta-sprite assembly / bounding box clustering (gemini: grouping nes sprites)
// TODO: consider implementing decay
/**
 * Represents either Primary or Secondary OAM
 */
public class ObjAttrMemory implements Nameable {

    public enum Kind {
        PRIMARY, SECONDARY
    }

    public static final int PRIMARY_ENTRY_COUNT = 64;
    public static final int SECONDARY_ENTRY_COUNT = 8; // default

    public static final int ENTRY_SIZE = 4; // bytes

    private final String name;
    private final Kind kind;

    private final @Unsigned byte[] buffer;
    private final @Unsigned byte mask;

    public ObjAttrMemory(String name, Kind kind, int count) {
        this.name = name;
        this.kind = kind;

        assertArgument(0 < count && count <= PRIMARY_ENTRY_COUNT, "count out of range");
        assertArgument(powOfTwo(count), "count is not power of 2");

        int size = count * ENTRY_SIZE;
        buffer = new byte[size];
        mask = ubyte(size - 1);
    }

    @Override
    public String getName() {
        return name;
    }

    public @Unsigned byte getStartAddress() {
        return UBYTE_0;
    }

    public @Unsigned byte getEndAddress() {
        return ubyte(buffer.length - 1);
    }

    public @Unsigned byte read(@Unsigned byte address) {
        int index = getIndex(address);
        @Unsigned byte data = buffer[index];
        return data;
    }

    private int getIndex(@Unsigned byte address) {
        return sint(address) & sint(mask);
    }

    public void write(@Unsigned byte address, @Unsigned byte data) {
        int index = getIndex(address);

        boolean isAttrByte = (index & 0b11) == 0b10;
        boolean isPrimary = kind == Kind.PRIMARY;

        int maybeUnsetUnused = isAttrByte && isPrimary ? ~0b11100 : 0xFF;
        int maybeWithoutUnused = sint(data) & maybeUnsetUnused;

        this.buffer[index] = ubyte(maybeWithoutUnused);
    }

    public int getSize() {
        return buffer.length;
    }

    public int getCount() {
        return buffer.length / ENTRY_SIZE;
    }

    @Override
    public String toString() {
        return name + " (" + Hex.s(getStartAddress()) + ":" + Hex.s(getEndAddress()) + ")";
    }
}
