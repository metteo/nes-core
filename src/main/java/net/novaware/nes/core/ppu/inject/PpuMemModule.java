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
import net.novaware.nes.core.ppu.table.AttributeTable;
import net.novaware.nes.core.ppu.table.NameTable;
import net.novaware.nes.core.ppu.table.PatternTable;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Quantity;

import static net.novaware.nes.core.config.VideoStandard.ACTIVE_HEIGHT;
import static net.novaware.nes.core.config.VideoStandard.ACTIVE_WIDTH;
import static net.novaware.nes.core.ppu.inject.PpuVarName.AT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.NT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PAL;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT1;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VRAM;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.ATTRIBUTE_TABLE_0_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.ATTRIBUTE_TABLE_0_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.NAME_TABLE_0_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.NAME_TABLE_0_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_MIRROR_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_SIZE;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PALETTE_RAM_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PATTERN_TABLE_0_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PATTERN_TABLE_0_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PATTERN_TABLE_1_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.PATTERN_TABLE_1_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.VRAM_START;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_1KB;
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;

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
            .allocatePhysicalBanks(() -> UBYTE_MAX_VALUE); // TODO: use configurable filler

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
    @PpuVar(OAM)
    static ObjAttrMemory provideObjAttrMemory() {
        return new ObjAttrMemory(OAM.doc(), ObjAttrMemory.SECONDARY_ENTRY_COUNT); // TODO: fill in a way all sprites are hidden and off screen on startup? / reset?
    }

    @Provides
    @BoardScope
    @PpuVar(DM)
    static DisplayMemory provideDisplayA() {
        // TODO: fill with regular black on reset?
        return new DisplayMemory(DM.doc(), ACTIVE_HEIGHT, ACTIVE_WIDTH);
    }

    @Binds
    @BoardScope
    @PpuVar(BUS)
    MemoryBus bindPpuBus(PpuBus ppuBus); // FIXME: seems like @BoardScope on PpuBus class doesn't work as it should

    @Provides
    @BoardScope
    @PpuVar(PT0)
    static SegmentRegister providePatternTable0Segment() {
        SegmentRegister segment = new SegmentRegister(PT0.name());
        segment.setStart(PATTERN_TABLE_0_START);
        segment.setEnd(PATTERN_TABLE_0_END);

        return segment;
    }

    @Provides
    @BoardScope
    @PpuVar(PT1)
    static SegmentRegister providePatternTable1Segment() {
        SegmentRegister segment = new SegmentRegister(PT1.name());
        segment.setStart(PATTERN_TABLE_1_START);
        segment.setEnd(PATTERN_TABLE_1_END);

        return segment;
    }

    @Provides
    @BoardScope
    @PpuVar(NT0)
    static SegmentRegister provideNameTable0Segment() {
        SegmentRegister segment = new SegmentRegister(NT0.name());
        segment.setStart(NAME_TABLE_0_START);
        segment.setEnd(NAME_TABLE_0_END);

        return segment;
    }

    @Provides
    @BoardScope
    @PpuVar(AT0)
    static SegmentRegister provideAttributeTable0Segment() {
        SegmentRegister segment = new SegmentRegister(AT0.name());
        segment.setStart(ATTRIBUTE_TABLE_0_START);
        segment.setEnd(ATTRIBUTE_TABLE_0_END);

        return segment;
    }

    // TODO: decide if table segments should be separate or @IntoCollection

    @Provides
    @BoardScope
    @PpuVar(PT0)
    static PatternTable providePatternTable0(
        @PpuVar(PT0) SegmentRegister segment,
        @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new PatternTable(PT0.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(PT1)
    static PatternTable providePatternTable1(
        @PpuVar(PT1) SegmentRegister segment,
        @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new PatternTable(PT1.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(NT0)
    static NameTable provideNameTable0(
        @PpuVar(NT0) SegmentRegister segment,
        @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new NameTable(NT0.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(AT0)
    static AttributeTable provideAttributeTable0(
            @PpuVar(AT0) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new AttributeTable(AT0.doc(), segment, ppuBus);
    }
}
