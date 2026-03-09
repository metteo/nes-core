package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface MemoryDevice extends AddressBus<DataBus>, DataBus {

    @Override
    default MemoryDevice specifyThen(@Unsigned short address) {
        specify(address);

        return this;
    }
}
