package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface MemoryBus extends AddressBus.Line, ControlBus.Line, DataBus.Line {

    void probe(@Unsigned short address, DataBus.Line dataLine);

    void attachCartridge(MemoryDevice.ReadWrite cartridge);
    void detachCartridge();

    void attachExpansion(MemoryDevice.ReadWrite expansion);
    void detachExpansion();
}
