package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class PhysicalMemory implements Addressable {

    private final UByteBuffer buffer;

    public PhysicalMemory(int size) {
        buffer = UByteBuffer.allocate(size);
    }

    @Override
    public @Unsigned byte read(@Unsigned short address) {
        final int position = uint(address);

        return buffer.position(position)
                .get();
    }

    @Override
    public void write(@Unsigned short address, @Unsigned byte data) {
        final int position = uint(address);

        buffer.position(position)
                .put(data);
    }
}
