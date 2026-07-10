package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.util.UTypes.UBYTE_MASK;

/**
 * Lightweight version of {@link ByteBuffer} without:
 *  - virtual calls (Heap vs Direct)
 *  - index checks apart from assert keyword
 *  - ordering
 *
 * Useful in hot path
 */
public class UByteBuffer {

    private final @Unsigned byte[] buffer;

    private UByteBuffer(int capacity) {
        this.buffer = new @Unsigned byte[capacity];
    }

    @SuppressWarnings("signedness")
    public static UByteBuffer of(ByteBuffer buffer) {
        var ubb = new UByteBuffer(buffer.capacity());
        buffer.get(ubb.buffer);

        return ubb;
    }

    public static UByteBuffer allocate(int capacity) {
        return new UByteBuffer(capacity);
    }

    public static UByteBuffer empty() {
        return allocate(0);
    }

    public @Unsigned byte get(int index) {
        return buffer[index];
    }

    public int getAsInt(int index) {
        return buffer[index] & UBYTE_MASK;
    }

//    public UByteBuffer get(int index, @Unsigned byte[] dst) {
//        buffer.get(index, dst);
//        return this;
//    }

    public void put(int index, @Unsigned byte b) {
        buffer[index] = b;
    }

    @SuppressWarnings("cast.unsafe")
    public void putAsByte(int index, int i) {
        buffer[index] = (@Unsigned byte) i;
    }


    public UByteBuffer put(int index, @Unsigned byte[] src) {
        // FIXME: assert on params

        System.arraycopy(src, 0, buffer, index, src.length);

        return this;
    }

    public int capacity() {
        return buffer.length;
    }

    public UByteBuffer fill(@Unsigned byte b) {
        return fill(() -> b);
    }

    public UByteBuffer fill(UByteSupplier supplier) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = supplier.getAsUByte();
        }

        return this;
    }

    @Override
    public String toString() {
        return "UByteBuffer[" + buffer.length + "]";
    }
}
