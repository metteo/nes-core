package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.memory.DataBus.*;
import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.USHORT_MAX_VALUE;
import static net.novaware.nes.core.util.UTypes.sint;

/**
 * Redirects calls to page specific device within index
 */
public class PagedMemory implements MemoryDevice.ReadWrite {

    private final MemoryDevice.ReadWrite openBus; // TODO: consider switching to a plug returning 0xFF for ANDing later

    private final MemoryDevice.ReadOnly[] readOnlyPages;
    private final MemoryDevice.WriteOnly[] writeOnlyPages;

    private @Unsigned short addressLatch;

    private MemoryDevice.ReadOnly readOnlyPageLatch;
    private MemoryDevice.WriteOnly writeOnlyPageLatch;

    private Line dataLine = new OpenLine();

    public PagedMemory(MemoryDevice.ReadWrite openBus) {
        this.openBus = openBus;

        final int length = 0xFF + 1;

        readOnlyPages = new MemoryDevice.ReadOnly[length];
        writeOnlyPages = new MemoryDevice.WriteOnly[length];

        for(int i = 0; i < length; i++) {
            readOnlyPages[i] = this.openBus;
            writeOnlyPages[i] = this.openBus;
        }

        readOnlyPageLatch = this.openBus;
        writeOnlyPageLatch = this.openBus;

        addressLatch = this.openBus.getEndAddress();
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
                if (memoryDevice instanceof MemoryDevice.ReadOnly readOnlyDevice) {
                    MemoryDevice.ReadOnly previousRead = readOnlyPages[page];
                    readOnlyPages[page] = readOnlyDevice;

                    if (previousRead != openBus) {
                        replaced.add(previousRead);
                    }
                }

                if (memoryDevice instanceof MemoryDevice.WriteOnly writeOnlyDevice) {
                    MemoryDevice.WriteOnly previousWrite = writeOnlyPages[page];
                    writeOnlyPages[page] = writeOnlyDevice;

                    if (previousWrite != openBus) {
                        replaced.add(previousWrite);
                    }
                }
            }
        }

        return replaced;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return USHORT_0;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return USHORT_MAX_VALUE;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        addressLatch = address;

        int page = (sint(address) & 0xFF00) >> 8;

        readOnlyPageLatch = readOnlyPages[page];
        writeOnlyPageLatch = writeOnlyPages[page];
    }

    @Override
    public void onRead() {
        readOnlyPageLatch.onAccess(addressLatch);
        readOnlyPageLatch.onRead();
    }

    @Override
    public void onWrite() {
        writeOnlyPageLatch.onAccess(addressLatch);
        writeOnlyPageLatch.onWrite();
    }

    @Override
    public void onAttach(Line dataLine) {
        this.dataLine = dataLine;
        onAttachPages(dataLine);
    }

    private void onAttachPages(Line dataLine) {
        for(int i = 0; i < readOnlyPages.length; i++) { // FIXME: call onAttach only for unique devices, one may be mapped to multiple pages
            readOnlyPages[i].onAttach(dataLine);
            writeOnlyPages[i].onAttach(dataLine);
        }
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
        onAttachPages(dataLine);
    }
}
