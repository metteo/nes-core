package net.novaware.nes.core.ppu;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;

public class Ppu {

    private final MemoryBus bus;

    private final PaletteMemory paletteMemory;
    private final ObjAttrMemory objAttrMemory;

    @Inject
    public Ppu(
        @Named("PPU.BUS") MemoryBus bus,
        PaletteMemory paletteMemory,
        ObjAttrMemory objAttrMemory
    ) {
        this.bus = bus;
        this.paletteMemory = paletteMemory;
        this.objAttrMemory = objAttrMemory;
    }
}
