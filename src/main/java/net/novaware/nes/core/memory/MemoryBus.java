package net.novaware.nes.core.memory;

public interface MemoryBus extends AddressBus.Line, ControlBus.Line, DataBus.Line {

    void attachCartridge(MemoryDevice.ReadWrite cartridge);
    void detachCartridge();

    void attachExpansion(MemoryDevice.ReadWrite expansion);
    void detachExpansion();
}
