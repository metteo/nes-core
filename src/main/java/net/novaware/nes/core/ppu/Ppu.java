package net.novaware.nes.core.ppu;

import jakarta.inject.Inject;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.register.PpuStatusRegister;

import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;

public class Ppu {

    private final MemoryBus bus;

    private final PpuStatusRegister status;
    private final PaletteMemory paletteMemory;
    private final ObjAttrMemory objAttrMemory;

    @Inject
    public Ppu(
        @PpuVar(BUS) MemoryBus bus,
        @PpuVar(PS) PpuStatusRegister status,
        PaletteMemory paletteMemory,
        ObjAttrMemory objAttrMemory
    ) {
        this.bus = bus;
        this.status = status;
        this.paletteMemory = paletteMemory;
        this.objAttrMemory = objAttrMemory;
    }
}
