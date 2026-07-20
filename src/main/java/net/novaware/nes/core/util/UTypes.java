package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

// TODO: consider value classes to create ubyte, ushort, uint with methods instead of checker @Unsigned
public final class UTypes {

    public static final @Unsigned short USHORT_0 = 0;
    public static final @Unsigned byte  UBYTE_0  = 0;

    public static final int USHORT_MASK = 0xFFFF; // FIXME: use in very hot code
    public static final int UBYTE_MASK  = 0xFF;   // FIXME: use in very hot code

    public static final @Unsigned short USHORT_MAX_VALUE = (@Unsigned short) USHORT_MASK;
    public static final @Unsigned byte  UBYTE_MAX_VALUE  = (@Unsigned byte)  UBYTE_MASK;

    /**
     * Convert unsigned short to signed int (regular int)
     */
    // TODO: replace signed shift >> with unsigned >>> everywhere if not troublesome with (@Unsigned int) / uint()
    public static int sint(@Unsigned short s) {
        return s & USHORT_MASK;
    }

    /**
     * Convert unsigned byte to signed int (regular int)
     */
    // TODO: replace signed shift >> with unsigned >>> everywhere if not troublesome with (@Unsigned int) / uint()
    public static int sint(@Unsigned byte b) {
        return b & UBYTE_MASK;
    }

    @SuppressWarnings("cast.unsafe")
    public static @Unsigned short ushort(int i) {
        return (@Unsigned short) i;
    }

    public static @Unsigned short ushort(@Unsigned byte b) {
        return (@Unsigned short) (b & UBYTE_MASK);
    }

    @SuppressWarnings("cast.unsafe")
    public static @Unsigned byte ubyte(int i) {
        return (@Unsigned byte) i;
    }

    @SuppressWarnings("cast.unsafe")
    public static @Unsigned byte ubyte(short s) {
        return (@Unsigned byte) s;
    }
}
