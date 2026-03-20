package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.stream.IntStream;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

public class MemoryPage implements MemoryDevice.ReadWrite { // TODO: test!

    private final int page;
    private final MemoryDevice.ReadWrite[] offsets;

    private final MemoryDevice.ReadWrite fallback;

    private MemoryDevice.ReadWrite offsetLatch;
    private @Unsigned short addressLatch;

    public MemoryPage(int page, MemoryDevice.ReadWrite fallback) {
        this.page = page & 0xFF;

        this.fallback = fallback; // TODO: validate that fallback covers whole page range?

        offsets = new MemoryDevice.ReadWrite[0xFF + 1];
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
    public void onAccess(@Unsigned short address) {
        // TODO: verify the page?

        offsetLatch = offsets[sint(address) & 0xFF];
        addressLatch = address;

        offsetLatch.onAccess(addressLatch);
    }

    @Override
    public void onRead() {
        offsetLatch.onRead();
    }

    @Override
    public void onWrite() {
        offsetLatch.onWrite();
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        offsetLatch.onAttach(dataLine); // TODO: attach to all devices, not only currently accessed
    }

    @Override
    public void onDetach() {
        offsetLatch.onDetach();
    }

    public void attach(MemoryDevice memoryDevice) {
        // FIXME: implement
    }
}
