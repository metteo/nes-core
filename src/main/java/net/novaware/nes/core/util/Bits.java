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

    // region 8 bits :: byte :: 0b1111_1111 :: 0xFF

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u8 {}
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s8 {} // default for byte

    // endregion
    // region 16 bits :: 2x byte :: char / short :: 0xFF_FF

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u16 {} // default for char
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s16 {} // default for short

    // endregion
    // region 32 bits :: 4x byte :: int

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u32 {}
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s32 {} // default for int

    // endregion
    // region 64 bits :: 8x byte :: long

    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface u64 {}
    @Documented @Retention(RUNTIME) @Target(TYPE_USE) public @interface s64 {} // default for long

    // endregion
}
