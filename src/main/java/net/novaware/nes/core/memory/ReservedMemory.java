package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

public class ReservedMemory implements MemoryDevice.ReadWrite, Nameable {

    private final String name;
    private final @Unsigned short startAddress;
    private final @Unsigned short endAddress;

    private @Unsigned short addressLatch;

    public ReservedMemory(String name, @Unsigned short startAddress, @Unsigned short endAddress) {
        this.name = name;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
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
    public void onAccess(@Unsigned short address) {
        addressLatch = address;

        throwException();
    }

    private void throwException() {
        throw new IllegalStateException("Reserved memory range accessed: " + Hex.s(addressLatch));
    }

    @Override
    public void onRead() {
        throwException();
    }

    @Override
    public void onWrite() {
        throwException();
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {}

    @Override
    public void onDetach() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + " (" + Hex.s(getStartAddress()) + ":" + Hex.s(getEndAddress()) + ")";
    }
}
