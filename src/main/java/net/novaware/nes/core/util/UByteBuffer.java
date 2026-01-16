package net.novaware.nes.core.util;

import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

// TODO: add trunction checks? e.g. if putting int as byte
public class UByteBuffer {

    private final ByteBuffer buffer;

    private UByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public static UByteBuffer of(ByteBuffer buffer) {
        return new UByteBuffer(buffer);
    }

    public static UByteBuffer allocate(int capacity) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        return of(buffer);
    }

    public static UByteBuffer empty() {
        return allocate(0);
    }

    public ByteBuffer unwrap() {
        return buffer;
    }

    @SuppressWarnings("signedness")
    public @Unsigned byte get() {
        return buffer.get();
    }

    public int getAsInt() {
        return get() & 0xFF;
    }

    @SuppressWarnings("signedness")
    public @Unsigned byte get(int index) {
        return buffer.get(index);
    }

    public int getAsInt(int index) {
        return get(index) & 0xFF;
    }

    /**
     * @see ByteBuffer#get(byte[])
     */
    @SuppressWarnings("signedness")
    public UByteBuffer get(@Unsigned byte[] dst) {
        buffer.get(dst);
        return this;
    }

    /**
     * @see ByteBuffer#get(int, byte[])
     */
    @SuppressWarnings("signedness")
    public UByteBuffer get(int index, @Unsigned byte[] dst) {
        buffer.get(index, dst);
        return this;
    }

    @SuppressWarnings("signedness")
    public UByteBuffer put(@Unsigned byte b) {
        buffer.put(b);
        return this;
    }

    @SuppressWarnings("signedness")
    public UByteBuffer put(@Unsigned byte[] bs) {
        buffer.put(bs);
        return this;
    }

    @SuppressWarnings("signedness")
    public UByteBuffer putAsByte(int i) {
        put((byte) i);
        return this;
    }

    @SuppressWarnings("signedness")
    public UByteBuffer put(int index, @Unsigned byte b) {
        buffer.put(index, b);
        return this;
    }

    @SuppressWarnings("signedness")
    public UByteBuffer put(int index, @Unsigned byte[] src) {
        buffer.put(index, src);
        return this;
    }

    public int position() {
        return buffer.position();
    }

    public UByteBuffer position(int newPosition) {
        buffer.position(newPosition);
        return this;
    }

    public UByteBuffer rewind() {
        buffer.rewind();
        return this;
    }

    public UByteBuffer order(ByteOrder order) {
        buffer.order(order);
        return this;

    }

    public int capacity() {
        return buffer.capacity();
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }
        UByteBuffer that = (UByteBuffer) o;
        return Objects.equals(buffer, that.buffer);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
