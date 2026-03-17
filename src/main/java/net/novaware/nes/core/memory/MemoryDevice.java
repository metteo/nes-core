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
}
