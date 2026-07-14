package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static net.novaware.nes.core.util.Asserts.assertArgument;
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

    public static final String MESSAGE_OUT_OF_BOUNDS = "index out of bounds";

    private final int capacity;
    private final @Unsigned byte[] buffer;

    private UByteBuffer(int capacity) {
        assertArgument(0 <= capacity, "capacity must be non-negative");

        this.capacity = capacity;
        this.buffer = new @Unsigned byte[capacity];
    }

    @SuppressWarnings("signedness")
    public static UByteBuffer of(ByteBuffer buffer) {
        var ubb = new UByteBuffer(buffer.capacity());
        buffer.get(0, ubb.buffer);

        return ubb;
    }

    public static UByteBuffer allocate(int capacity) {
        return new UByteBuffer(capacity);
    }

    public static UByteBuffer empty() {
        return allocate(0);
    }

    public @Unsigned byte get(int index) {
        assert isWithinBounds(index) : MESSAGE_OUT_OF_BOUNDS;

        return buffer[index];
    }

    private boolean isWithinBounds(int index) {
        return 0 <= index && index <= capacity;
    }

    public int getAsInt(int index) {
        assert isWithinBounds(index) : MESSAGE_OUT_OF_BOUNDS;

        return buffer[index] & UBYTE_MASK;
    }

    public void put(int index, @Unsigned byte b) {
        assert isWithinBounds(index) : MESSAGE_OUT_OF_BOUNDS;

        buffer[index] = b;
    }

    @SuppressWarnings("signedness")
    public void putAsByte(int index, int i) {
        assert isWithinBounds(index) : MESSAGE_OUT_OF_BOUNDS;

        buffer[index] = (byte) i;
    }

    public int capacity() {
        return capacity;
    }

    @SuppressWarnings("signedness")
    public UByteBuffer fill(@Unsigned byte b) {
        Arrays.fill(buffer, b);

        return this;
    }

    public UByteBuffer fill(UByteSupplier supplier) {
        for (int i = 0; i < capacity; i++) {
            buffer[i] = supplier.getAsUByte();
        }

        return this;
    }

    public UByteBuffer fill(int index, @Unsigned byte[] src) {
        assert isWithinBounds(index) : MESSAGE_OUT_OF_BOUNDS;

        System.arraycopy(src, 0, buffer, index, src.length);

        return this;
    }

    @Override
    public String toString() {
        return "UByteBuffer[cap=" + capacity + "]";
    }
}
