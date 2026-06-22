package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.UByteBuffer;
import net.novaware.nes.core.util.UByteSupplier;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Nums.powOfTwo;
import static net.novaware.nes.core.util.UTypes.sint;

/**
 * TODO: write a Javadoc about size vs end address
 */
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

        int size = buffer.capacity();
        if (sint(endAddress) - sint(startAddress) + 1 == size) {
            this.mask = 0xFFFF;
        } else {
            assertArgument(
                size > 0 && powOfTwo(size),
                "size must be a power of two to allow mirroring with mask"
            );
            this.mask = size - 1;
        }
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

    private int toPosition(@Unsigned short address) {
        // TODO: check how validation of address within range will affect performance (always vs assert)
        assert sint(startAddress) <= sint(address) && sint(address) <= sint(endAddress) : "address out of range";

        return (sint(address) - sint(startAddress)) & mask;
    }

    @Override
    public void probe(@Unsigned short address, DataBus.Line dataLine) {
        assert sint(startAddress) <= sint(address) && sint(address) <= sint(endAddress);

        int position = toPosition(address);
        @Unsigned byte data = buffer.get(position);

        dataLine.data(data);
    }

    private @Unsigned byte readByte() {
        return buffer.get(position); // get() advances position, we want to keep it
    }

    private void writeByte(@Unsigned byte data) {
        buffer.put(position, data);
    }

    @Override
    public void onAccess(@Unsigned short address) {
        position = toPosition(address);

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

    @Override
    public String toString() {
        return name + " (" + Hex.s(startAddress) + ":" + Hex.s(endAddress) + ")";
    }

    public PhysicalMemory fill(UByteSupplier supplier) {
        buffer.fill(supplier);
        return this;
    }
}
