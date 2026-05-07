package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.memory.DataBus.Line;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * Redirects calls to page specific device within index
 */
public class PagedMemory implements MemoryDevice.ReadWrite, Nameable {

    private final String name;
    private final int lastPage;
    private final MemoryDevice.ReadWrite fallback;

    private final List<MemoryDevice> devices = new ArrayList<>();

    private final MemoryDevice.ReadOnly[] readPages;
    private final MemoryDevice.WriteOnly[] writePages;

    private @Unsigned short addressLatch;

    private MemoryDevice.ReadOnly readPageLatch;
    private MemoryDevice.WriteOnly writePageLatch;

    private Line dataLine = new OpenLine();

    public PagedMemory(String name, int size, MemoryDevice.ReadWrite fallback) {
        this.name = name;
        this.lastPage = (0xFF00 & (size - 1)) >> 8;
        this.fallback = fallback;

        final int length = lastPage + 1;

        readPages = new MemoryDevice.ReadOnly[length];
        writePages = new MemoryDevice.WriteOnly[length];

        for(int i = 0; i < length; i++) {
            readPages[i] = this.fallback;
            writePages[i] = this.fallback;
        }

        readPageLatch = this.fallback;
        writePageLatch = this.fallback;

        addressLatch = this.fallback.getEndAddress();
    }

    @Override
    public @Unsigned short getStartAddress() {
        return USHORT_0;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return ushort((lastPage << 8) | 0xFF);
    }

    @SuppressWarnings("not.interned") // comparing refs on purpose
    public void attach(MemoryDevice memoryDevice) {
        devices.add(memoryDevice);

        for (int page = 0; page <= lastPage; page++) {
            int pageStart = (page << 8);
            int pageEnd = pageStart | 0xFF;

            int deviceStart = sint(memoryDevice.getStartAddress());
            int deviceEnd = sint(memoryDevice.getEndAddress());

            if (deviceStart <= pageStart && pageEnd <= deviceEnd) {
                if (memoryDevice instanceof MemoryDevice.ReadOnly readDevice) {
                    MemoryDevice.ReadOnly previousRead = readPages[page];

                    assertArgument(previousRead == fallback, "Attempting to replace R " + previousRead +
                            " with " + readDevice);

                    readPages[page] = readDevice;
                }

                if (memoryDevice instanceof MemoryDevice.WriteOnly writeDevice) {
                    MemoryDevice.WriteOnly previousWrite = writePages[page];

                    assertArgument(previousWrite == fallback, "Attempting to replace W " + previousWrite +
                            " with " + writeDevice);

                    writePages[page] = writeDevice;
                }
            }
        }
    }

    @SuppressWarnings("not.interned") // comparing refs on purpose
    public void detach(MemoryDevice memoryDevice) {
        devices.remove(memoryDevice);

        for (int page = 0; page <= lastPage; page++) {
            if (readPages[page] == memoryDevice) {
                readPages[page] = fallback;
            }

            if (writePages[page] == memoryDevice) {
                writePages[page] = fallback;
            }
        }
    }

    @Override
    public void onAccess(@Unsigned short address) {
        addressLatch = address;

        int page = (sint(address) & 0xFF00) >> 8;

        readPageLatch = readPages[page];
        writePageLatch = writePages[page];
    }

    @Override
    public void onRead() {
        readPageLatch.onAccess(addressLatch);
        readPageLatch.onRead();
    }

    @Override
    public void onWrite() {
        writePageLatch.onAccess(addressLatch);
        writePageLatch.onWrite();
    }

    @Override
    public void onAttach(Line dataLine) {
        this.dataLine = dataLine;
        onAttachPages(dataLine);
    }

    private void onAttachPages(Line dataLine) {
        devices.stream()
                .filter(d -> d instanceof DataBus.Device)
                .map(d -> (DataBus.Device) d)
                .forEach(d -> d.onAttach(dataLine));
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
        onAttachPages(dataLine);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + Hex.s(getStartAddress()) + ":" + Hex.s(getEndAddress()) + ")";
    }

    // @VisibleForTesting
    /* package */ int getAllocatedPageCount() {
        return readPages.length + writePages.length;
    }
}
