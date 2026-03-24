package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_START;

public interface MemoryDevice extends AddressBus.Device {

    @Unsigned short getStartAddress();

    @Unsigned short getEndAddress();

    interface ReadOnly extends MemoryDevice, ControlBus.ReadOnlyDevice, DataBus.Device {}
    interface WriteOnly extends MemoryDevice, ControlBus.WriteOnlyDevice, DataBus.Device {}
    interface ReadWrite extends ReadOnly, WriteOnly {}

    class Empty implements ReadWrite, MemoryDevice {
        @Override public @Unsigned short getStartAddress() { return MEMORY_START; }
        @Override public @Unsigned short getEndAddress() { return MEMORY_END; }
        @Override public void onAccess(@Unsigned short address) {}
        @Override public void onRead() {}
        @Override public void onWrite() {}
        @Override public void onAttach(DataBus.Line dataLine) {}
        @Override public void onDetach() {}
        @Override public String toString() { return "EMPTY"; }
    }
}
