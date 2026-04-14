package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.USHORT_MAX_VALUE;

public interface MemoryDevice extends AddressBus.Device {

    @Unsigned short getStartAddress();

    @Unsigned short getEndAddress();

    interface ReadOnly extends MemoryDevice, ControlBus.ReadOnlyDevice, DataBus.Device {}
    interface WriteOnly extends MemoryDevice, ControlBus.WriteOnlyDevice, DataBus.Device {}
    interface ReadWrite extends ReadOnly, WriteOnly {}

    class Empty implements ReadWrite, MemoryDevice { // TODO: consider 16 and 14 bit variants
        @Override public @Unsigned short getStartAddress() { return USHORT_0; }
        @Override public @Unsigned short getEndAddress() { return USHORT_MAX_VALUE; }
        @Override public void onAccess(@Unsigned short address) {}
        @Override public void onRead() {}
        @Override public void onWrite() {}
        @Override public void onAttach(DataBus.Line dataLine) {}
        @Override public void onDetach() {}
        @Override public String toString() { return "EMPTY"; }
    }

    // TODO: maybe single instance of empty?
}
