package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface MemoryBus extends AddressBus<DataBus>, DataBus {

    @Override
    default MemoryBus specifyThen(@Unsigned short address) {
        specify(address);

        return this;
    }

    void attach(MemoryDevice memoryDevice);

}
