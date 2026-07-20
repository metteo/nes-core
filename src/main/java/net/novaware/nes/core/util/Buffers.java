package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.util.UTypes.UBYTE_MASK;

public class Buffers {

    @SuppressWarnings("signedness")
    public static @Unsigned byte get(ByteBuffer buffer, int index) {
        return buffer.get(index);
    }

    public static int getAsInt(ByteBuffer buffer, int index) {
        return buffer.get(index) & UBYTE_MASK;
    }

    @SuppressWarnings("signedness")
    public static void get(ByteBuffer buffer, int index, @Unsigned byte[] dst) {
        buffer.get(index, dst);
    }

    @SuppressWarnings("signedness")
    public static void put(ByteBuffer buffer, int index, @Unsigned byte b) {
        buffer.put(index, b);
    }

    public static void putAsByte(ByteBuffer buffer, int index, int i) {
        buffer.put(index, (byte) i);
    }

    @SuppressWarnings("signedness")
    public static void put(ByteBuffer buffer, int index, @Unsigned byte[] src) {
        buffer.put(index, src);
    }

    public static ByteBuffer fill(ByteBuffer buffer, @Unsigned byte b) {
        return fill(buffer, () -> b);
    }

    @SuppressWarnings("signedness")
    public static ByteBuffer fill(ByteBuffer buffer, UByteSupplier supplier) {
        buffer.clear();

        while (buffer.hasRemaining()) {
            buffer.put(supplier.getAsUByte());
        }

        return buffer.clear();
    }
}
