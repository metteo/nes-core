package net.novaware.nes.core.ppu.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.memory.BankedMemory;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.memory.PpuBus;
import net.novaware.nes.core.util.Quantity;

import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VRAM;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_MIRROR_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_SIZE;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.VRAM_START;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_1KB;

@Module
public interface PpuMemModule {

    @Provides
    @BoardScope
    @PpuVar(VRAM)
    static BankedMemory provideVideoMemory() {
        BankedMemory videoMemory = new BankedMemory(
            VRAM.doc(),
            VRAM_START,
            new Quantity(1, BANK_1KB)
        )
            .setPhysicalBanks(new Quantity(2, BANK_1KB))
            .allocatePhysicalBanks();

        return videoMemory;
    }

    @Provides
    @BoardScope
    static PaletteMemory paletteMemory() {
        return new PaletteMemory(
            "Palette RAM",
            PALETTE_RAM_START,
            PALETTE_RAM_MIRROR_END,
            PALETTE_RAM_SIZE
        );
    }

    @Binds
    @BoardScope
    @PpuVar(BUS)
    MemoryBus bindPpuBus(PpuBus ppuBus);
}
