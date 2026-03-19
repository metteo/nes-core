package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.MEMORY_START;
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;

public interface MemoryDevice extends AddressBus<MemoryDevice>, DataBus {

    @Unsigned short getStartAddress();

    @Unsigned short getEndAddress();

    @Override
    default MemoryDevice specifyThen(@Unsigned short address) {
        specify(address);

        return this;
    }

    interface AccessOnly extends AddressBus.Device { // TODO: make this just MemoryDevice^ when old methods are removed
        @Unsigned short getStartAddress();

        @Unsigned short getEndAddress();
    }

    interface ReadOnly extends AccessOnly, ControlBus.ReadOnlyDevice, DataBus.Device {

    }

    interface WriteOnly extends AccessOnly, ControlBus.WriteOnlyDevice, DataBus.Device {

    }

    interface ReadWrite extends ReadOnly, WriteOnly {

    }

    class EmptyDevice implements ReadWrite, MemoryDevice {
        @Override public @Unsigned short getStartAddress() { return MEMORY_START; }
        @Override public @Unsigned short getEndAddress() { return MEMORY_END; }
        @Override public void onAccess(@Unsigned short address) {}
        @Override public void onRead() {}
        @Override public void onWrite() {}
        @Override public void onAttach(DataBus.Line dataLine) {}
        @Override public void onDetach() {}

        // TODO: remove these
        @Override public void specify(@Unsigned short address) {}
        @Override public @Unsigned byte readByte() { return UBYTE_MAX_VALUE; }
        @Override public void writeByte(@Unsigned byte data) {}
    }
}
