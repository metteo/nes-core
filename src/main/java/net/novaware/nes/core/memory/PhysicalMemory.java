package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;

public class PhysicalMemory implements MemoryDevice {

    private final UByteBuffer buffer;
    private final int offset;

    public PhysicalMemory(int size, int offset) {
        buffer = UByteBuffer.allocate(size);
        this.offset = offset;
    }

    public PhysicalMemory(int size) {
        this(size, 0);
    }

    public PhysicalMemory(UByteBuffer buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }

    public PhysicalMemory(UByteBuffer buffer) {
        this(buffer, 0);
    }


    @Override
    public void specify(@Unsigned short address) {
        final int position = sint(address) - offset;

        buffer.position(position);
    }

    @Override
    public @Unsigned byte readByte() {
        return buffer.get();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        buffer.put(data);
    }
}
