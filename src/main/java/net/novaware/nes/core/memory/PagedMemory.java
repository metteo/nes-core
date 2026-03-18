package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.util.UTypes.sint;

/**
 * Redirects calls to page specific device within index
 */
// TODO: rename to PagedBus
public class PagedMemory implements AddressBus.Line, ControlBus, ControlBus.Line, DataBus.Line { // TODO: switch to MemoryBus

    private final MemoryDevice.ReadWrite openBus;

    private BusOp busOp = BusOp.DATA_READ; // TODO: randomize between data read / write

    private final MemoryDevice.ReadOnly[] readOnlyPages;
    private final MemoryDevice.WriteOnly[] writeOnlyPages;

    private @Unsigned short addressLatch;

    private MemoryDevice.ReadOnly readOnlyPageLatch;
    private MemoryDevice.WriteOnly writeOnlyPageLatch;

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
    public List<MemoryDevice.AccessOnly> attach(MemoryDevice.AccessOnly memoryDevice) {
        List<MemoryDevice.AccessOnly> replaced = new ArrayList<>();

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
    public ControlBus.Line access(@Unsigned short address) {
        assert busOp == BusOp.DATA_READ || busOp == BusOp.DATA_WRITE; // compile out, TODO: consider JCP or Manifold

        busOp = BusOp.ADDRESS_ACCESS;
        addressLatch = address;

        int page = (sint(address) & 0xFF00) >> 8;

        readOnlyPageLatch = readOnlyPages[page];
        writeOnlyPageLatch = writeOnlyPages[page];

        return this;
    }

    @Override
    public DataBus.Read read() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out
        readOnlyPageLatch.onAccess(addressLatch);

        busOp = BusOp.CONTROL_READ;

        return this;
    }

    @Override
    public DataBus.Write write() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out
        writeOnlyPageLatch.onAccess(addressLatch);

        busOp = BusOp.CONTROL_WRITE;

        return this;
    }

    @Override
    public @Unsigned byte data() {
        assert busOp == BusOp.CONTROL_READ; // compile out

        busOp = BusOp.DATA_READ;

        @Unsigned byte data = readOnlyPageLatch.onRead();
        openBus.onWrite(data); // keep the last value

        return data;
    }



    @Override
    public void data(@Unsigned byte data) {
        assert busOp == BusOp.CONTROL_WRITE; // compile out

        busOp = BusOp.DATA_WRITE;

        openBus.onWrite(data);
        writeOnlyPageLatch.onWrite(data); // might be openBus too, no problem
    }

    @Override
    public BusOp currentOp() {
        return busOp;
    }
}
