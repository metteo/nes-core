package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;

public class PhysicalMemory implements MemoryDevice, MemoryDevice.ReadWrite, Nameable {

    private final String name;

    private final @Unsigned short startAddress;
    private final @Unsigned short endAddress;

    private final UByteBuffer buffer;
    private final int mask;

    private int position;

    private DataBus.Line dataLine = new OpenLine();

    public PhysicalMemory(
            String name,
            @Unsigned short startAddress,
            @Unsigned short endAddress,
            UByteBuffer buffer
    ) {
        this.name = name;
        this.startAddress = startAddress;
        this.endAddress = endAddress;

        this.buffer = buffer;
        this.mask = buffer.capacity() - 1;
    }

    public PhysicalMemory(
        String name,
        @Unsigned short startAddress,
        @Unsigned short endAddress,
        int size
    ) {
        this(name, startAddress, endAddress, UByteBuffer.allocate(size));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return startAddress;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return endAddress;
    }

    public @Unsigned byte readByte() {
        return buffer.get(position); // get() advances position, we want to keep it
    }

    public void writeByte(@Unsigned byte data) {
        buffer.put(position, data);
    }

    @Override
    public void onAccess(@Unsigned short address) {
        // TODO: check how validation of address within range will affect performance (always vs assert)
        position = (sint(address) - sint(startAddress)) & mask;

        buffer.position(position);
    }

    @Override
    public void onRead() {
        dataLine.data(readByte());
    }

    @Override
    public void onWrite() {
        writeByte(dataLine.data());
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
    }
}
