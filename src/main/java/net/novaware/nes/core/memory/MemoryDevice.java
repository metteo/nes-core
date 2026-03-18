package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

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

    interface ReadOnly extends AccessOnly, DataBus.ReadOnlyDevice {

    }

    interface WriteOnly extends AccessOnly, DataBus.WriteOnlyDevice {

    }

    interface ReadWrite extends AccessOnly, ReadOnly, WriteOnly {

    }
}
