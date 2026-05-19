package net.novaware.nes.core.ppu.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.memory.BankedMemory;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.memory.DisplayMemory;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.memory.PpuBus;
import net.novaware.nes.core.util.Quantity;

import static net.novaware.nes.core.config.VideoStandard.ACTIVE_HEIGHT;
import static net.novaware.nes.core.config.VideoStandard.ACTIVE_WIDTH;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DBM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PAL;
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
    static PaletteMemory providePaletteMemory() {
        return new PaletteMemory(
            PAL.doc(),
            PALETTE_RAM_START,
            PALETTE_RAM_MIRROR_END,
            PALETTE_RAM_SIZE
        );
    }

    @Provides
    @BoardScope
    static ObjAttrMemory provideObjAttrMemory() {
        return new ObjAttrMemory(OAM.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(DAM)
    static DisplayMemory provideDisplayA() {
        return new DisplayMemory(DAM.doc(), ACTIVE_HEIGHT, ACTIVE_WIDTH);
    }

    @Provides
    @BoardScope
    @PpuVar(DBM)
    static DisplayMemory provideDisplayB() {
        return new DisplayMemory(DBM.doc(), ACTIVE_HEIGHT, ACTIVE_WIDTH);
    }

    @Binds
    @BoardScope
    @PpuVar(BUS)
    MemoryBus bindPpuBus(PpuBus ppuBus);
}
