package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;

public class IndexedMemory implements MemoryDevice {

    enum AddressPart {
        PAGE,  // high byte
        OFFSET // low byte
    }

    private final MemoryDevice[] devices;
    private final MemoryDevice[] index;

    private final int mask;
    private final int shift;

    private final @Unsigned short startAddress;
    private final @Unsigned short endAddress;

    private MemoryDevice currentDevice;

    public IndexedMemory(AddressPart part, MemoryDevice... devices) {
        switch (part) {
            case PAGE -> {
                mask = 0xFF00;
                shift = 8;
            }
            case OFFSET -> {
                mask = 0x00FF;
                shift = 0;
            }

            default -> throw new IllegalArgumentException("Unknown address part: " + part);
        }

        this.devices = devices; // TODO: make a copy
        this.currentDevice = devices[0];
        // TODO: verify there is no overlap

        startAddress = devices[0].getStartAddress();
        endAddress = devices[devices.length - 1].getEndAddress();

        index = new MemoryDevice[0xFF + 1];
        for (int i = 0; i <= 0xFF; i++) {
            for (MemoryDevice device : this.devices) {

                int start = (sint(device.getStartAddress()) & mask) >> shift;
                int end = (sint(device.getEndAddress()) & mask) >> shift;

                if (start <= i && i <= end) {
                    if (index[i] != null) {
                        throw new IllegalStateException("Overlapping memory " + part + "s!");
                    } else {
                        index[i] = device;
                    }
                }
            }

            if (index[i] == null) {
                throw new IllegalStateException("Undefined memory " + part + "!"); // TODO: add number
            }
        }

        // TODO: validate memory continuity
    }

    @Override
    public @Unsigned short getStartAddress() {
        return startAddress;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return endAddress;
    }

    @Override
    public void specify(@Unsigned short address) {
        int page = (sint(address) & mask) >> shift;

        currentDevice = index[page];
        currentDevice.specify(address);
    }

    @Override
    public @Unsigned byte readByte() {
        return currentDevice.readByte();
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        currentDevice.writeByte(data);
    }
}
