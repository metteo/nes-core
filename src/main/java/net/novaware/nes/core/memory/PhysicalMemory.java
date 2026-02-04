package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class PhysicalMemory implements Addressable {

    private final UByteBuffer buffer;
    private final int offset;

    public PhysicalMemory(int size, int offset) {
        buffer = UByteBuffer.allocate(size);
        this.offset = offset;
    }

    public PhysicalMemory(int size) {
        this(size, 0);
    }

    @Override
    public @Unsigned byte read(@Unsigned short address) {
        final int position = uint(address) - offset;

        return buffer.position(position)
                .get();
    }

    @Override
    public void write(@Unsigned short address, @Unsigned byte data) {
        final int position = uint(address) - offset;

        buffer.position(position)
                .put(data);
    }
}
