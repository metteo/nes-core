package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.stream.IntStream;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

public class MemoryPage implements MemoryDevice { // TODO: test!

    private final int page;
    private final MemoryDevice[] offsets;

    private final MemoryDevice fallback;

    private MemoryDevice offsetLatch;
    private @Unsigned short addressLatch;

    public MemoryPage(int page, MemoryDevice fallback) {
        this.page = page & 0xFF;

        this.fallback = fallback; // TODO: validate that fallback covers whole page range?

        offsets = new MemoryDevice[0xFF + 1];
        IntStream.range(0, offsets.length)
                .forEach(i -> offsets[i] = this.fallback);

        offsetLatch = this.fallback;
        addressLatch = ushort(page << 8);
    }

    @Override
    public @Unsigned short getStartAddress() {
        return ushort(page << 8);
    }

    @Override
    public @Unsigned short getEndAddress() {
        return ushort((page << 8) | 0xFF);
    }

    @Override
    public void specify(@Unsigned short address) {
        // TODO: verify the page?

        offsetLatch = offsets[sint(address) & 0xFF];
        addressLatch = address;

        offsetLatch.specify(addressLatch);
    }

    @Override
    public @Unsigned byte readByte() {
        return offsetLatch.readByte();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        offsetLatch.writeByte(data);
    }
}
