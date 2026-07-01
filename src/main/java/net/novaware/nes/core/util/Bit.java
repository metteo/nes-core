package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.Nullable;

import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * Represents a single semantic bit (0 or 1).
 * Memory-efficient, branchless, and completely garbage-free.
 *
 * @author gemini
 */
public final class Bit {

    public static final Bit ZERO = new Bit(0);
    public static final Bit ONE = new Bit(1);

    private final @Signed byte value;

    private Bit(int value) {
        this.value = (byte) value;
    }

    public static Bit of(int value) {
        return BitCache.VALUES[value & 0b1];
    }

    public int toInt() {
        return value;
    }

    public @Unsigned byte toUByte() {
        return ubyte((int) value);
    }

    public Bit not() {
        return BitCache.VALUES[value ^ 1];
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj; // Safe because of strict caching
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private static final class BitCache {
        static final Bit[] VALUES = { ZERO, ONE };
    }
}
