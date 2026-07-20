package net.novaware.nes.core.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Uses Kaitai integer type naming
 */
public final class Bits {

    private Bits() {}

    // region 8 bits :: byte

    public static final int MIN_U8 = 0;
    public static final int MAX_U8 = 0xFF;

    public static final int MIN_S8 = Byte.MIN_VALUE;
    public static final int MAX_S8 = Byte.MAX_VALUE;

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u8 {}
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s8 {} // default for byte

    // endregion
    // region 16 bits :: 2x byte :: char / short

    public static final int MIN_U16 = 0;
    public static final int MAX_U16 = 0xFFFF;

    public static final int MIN_S16 = Short.MIN_VALUE;
    public static final int MAX_S16 = Short.MAX_VALUE;

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u16 {} // default for char
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s16 {} // default for short

    // endregion
    // region 32 bits :: 4x byte :: int

    public static final int MIN_U32 = 0;
    public static final int MAX_U32 = (int) 0xFFFF_FFFFL;

    public static final int MIN_S32 = Integer.MIN_VALUE;
    public static final int MAX_S32 = Integer.MAX_VALUE;

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u32 {}
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s32 {} // default for int

    // endregion
    // region 64 bits :: 8x byte :: long

    public static final long MIN_U64 = 0;
    public static final long MAX_U64 = -1L;

    public static final long MIN_S64 = Long.MIN_VALUE;
    public static final long MAX_S64 = Long.MAX_VALUE;

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u64 {}
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s64 {} // default for long

    // endregion
}
