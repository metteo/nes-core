package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface MemoryBus extends
        AddressBus<MemoryBus>, ControlBus, DataBus, // old, remove
        AddressBus.Line, ControlBus.Line, DataBus.Line // new, keep
{

    enum Type {
        STANDARD,
        RECORDING
    }

    @Override
    default MemoryBus specifyThen(@Unsigned short address) {
        specify(address);

        return this;
    }

    void attachCartridge(MemoryDevice.ReadWrite cartridge);
    void detachCartridge();

    void attachExpansion(MemoryDevice.ReadWrite expansion);
    void detachExpansion();
}
