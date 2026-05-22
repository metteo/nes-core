package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

public class MemoryPage implements MemoryDevice.ReadWrite {

    private final MemoryDevice.ReadWrite fallback;

    private final List<MemoryDevice> devices = new ArrayList<>();

    private final int page;
    private final MemoryDevice.ReadOnly[] readOffsets;
    private final MemoryDevice.WriteOnly[] writeOffsets;

    private @Unsigned short addressLatch;

    private MemoryDevice.ReadOnly readOffsetLatch;
    private MemoryDevice.WriteOnly writeOffsetLatch;

    private DataBus.Line dataLine = new OpenLine();

    public MemoryPage(@Unsigned byte page, MemoryDevice.ReadWrite fallback) {
        this.page = sint(page);

        this.fallback = fallback;

        readOffsets = new MemoryDevice.ReadOnly[0xFF + 1];
        writeOffsets = new MemoryDevice.WriteOnly[0xFF + 1];
        
        IntStream.range(0, readOffsets.length).forEach(i -> {
            readOffsets[i] = this.fallback;
            writeOffsets[i] = this.fallback;
        });

        readOffsetLatch = this.fallback;
        writeOffsetLatch = this.fallback;
        addressLatch = ushort(this.page << 8);
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
    public void probe(@Unsigned short address, DataBus.Line dataLine) {
        assert sint(getStartAddress()) <= sint(address) && sint(address) <= sint(getEndAddress());

        readOffsets[toOffset(address)].probe(address, dataLine);
    }

    @SuppressWarnings("not.interned")
    public void attach(MemoryDevice memoryDevice) {
        devices.add(memoryDevice);

        for (int offset = 0; offset <= 0xFF; offset++) {
            int address = (page << 8) | offset;

            if (address >= sint(memoryDevice.getStartAddress()) && address <= sint(memoryDevice.getEndAddress())) {
                if (memoryDevice instanceof MemoryDevice.ReadOnly readDevice) {
                    MemoryDevice.ReadOnly previousRead = readOffsets[offset];

                    assertArgument(previousRead == fallback, "Attempting to replace R " + previousRead +
                            " with " + readDevice);

                    readOffsets[offset] = readDevice;
                }

                if (memoryDevice instanceof MemoryDevice.WriteOnly writeDevice) {
                    MemoryDevice.WriteOnly previousWrite = writeOffsets[offset];

                    assertArgument(previousWrite == fallback, "Attempting to replace W " + previousWrite +
                            " with " + writeDevice);

                    writeOffsets[offset] = writeDevice;
                }
            }
        }
    }

    @Override
    public void onAccess(@Unsigned short address) {
        addressLatch = address;

        int offset = toOffset(address);

        readOffsetLatch = readOffsets[offset];
        writeOffsetLatch = writeOffsets[offset];
    }

    private int toOffset(@Unsigned short address) {
        return sint(address) & 0xFF;
    }

    @Override
    public void onRead() {
        readOffsetLatch.onAccess(addressLatch);
        readOffsetLatch.onRead();
    }

    @Override
    public void onWrite() {
        writeOffsetLatch.onAccess(addressLatch);
        writeOffsetLatch.onWrite();
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
        onAttachOffsets(dataLine);
    }

    private void onAttachOffsets(DataBus.Line dataLine) {
        devices.stream()
                .filter(d -> d instanceof DataBus.Device)
                .map(d -> (DataBus.Device) d)
                .forEach(d -> d.onAttach(dataLine));
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
        onAttachOffsets(dataLine);
    }

    @Override
    public String toString() {
        return "PAGE 0x" + Hex.s(ubyte(page)) +
                " (" + Hex.s(getStartAddress()) + ":" + Hex.s(getEndAddress()) + ")";
    }
}
