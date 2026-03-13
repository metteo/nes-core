package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static net.novaware.nes.core.util.UTypes.sint;

/**
 * Redirects calls to page specific device within index
 */
public class PagedMemory implements MemoryDevice {

    private final MemoryDevice[] pages;

    private final MemoryDevice fallback;

    private MemoryDevice pageLatch;
    private @Unsigned short addressLatch;

    public PagedMemory(MemoryDevice fallback) {
        this.fallback = fallback; // TODO: validate that fallback covers whole memory range?

        pages = new MemoryDevice[0xFF + 1];
        IntStream.range(0, pages.length)
                .forEach(i -> pages[i] = this.fallback);

        pageLatch = this.fallback;
        addressLatch = this.fallback.getEndAddress();
    }

    @SuppressWarnings("not.interned")
    public List<MemoryDevice> attach(MemoryDevice memoryDevice) {
        List<MemoryDevice> replaced = new ArrayList<>();

        for (int page = 0; page <= 0xFF; page++) {
            int pageStart = (page << 8);
            int pageEnd = pageStart | 0xFF;

            int deviceStart = sint(memoryDevice.getStartAddress());
            int deviceEnd = sint(memoryDevice.getEndAddress());

            if (deviceStart <= pageStart && pageEnd <= deviceEnd) {
                MemoryDevice previous = pages[page];
                pages[page] = memoryDevice;

                if (previous != fallback) {
                    replaced.add(previous);
                }
            }
        }

        return replaced;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return pages[0].getStartAddress();
    }

    @Override
    public @Unsigned short getEndAddress() {
        return pages[pages.length - 1].getEndAddress();
    }

    @Override
    public void specify(@Unsigned short address) {
        int page = (sint(address) & 0xFF00) >> 8;

        pageLatch = pages[page];
        addressLatch = address;

        pageLatch.specify(addressLatch);
    }

    @Override
    public @Unsigned byte readByte() {
        return pageLatch.readByte();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        pageLatch.writeByte(data);
    }
}
