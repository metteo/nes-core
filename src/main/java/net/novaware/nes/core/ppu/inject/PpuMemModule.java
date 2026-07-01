package net.novaware.nes.core.ppu.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.memory.BankedMemory;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.memory.PaletteMemory;
import net.novaware.nes.core.ppu.memory.PpuBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Quantity;

import static net.novaware.nes.core.ppu.inject.PpuVarName.AT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LTS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PAL;
import static net.novaware.nes.core.ppu.inject.PpuVarName.POA;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT1;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PTS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SOA;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VRAM;
import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.Kind.PRIMARY;
import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.Kind.SECONDARY;
import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.PRIMARY_ENTRY_COUNT;
import static net.novaware.nes.core.ppu.memory.ObjAttrMemory.SECONDARY_ENTRY_COUNT;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.ATTRIBUTE_TABLE_0_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.ATTRIBUTE_TABLE_0_START;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.ATTRIBUTE_TABLE_3_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.LAYOUT_TABLE_0_END;
import static net.novaware.nes.core.ppu.memory.PpuMemMap.LAYOUT_TABLE_0_START;
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
    @PpuVar(POA)
    static ObjAttrMemory providePrimaryObjAttrMemory() {
        return new ObjAttrMemory(POA.doc(), PRIMARY, PRIMARY_ENTRY_COUNT);
    }

    @Provides
    @BoardScope
    @PpuVar(SOA)
    static ObjAttrMemory provideSecondaryObjAttrMemory() {
        // TODO: add support for 2x or 4x more sprites in a line to prevent flicker
        return new ObjAttrMemory(SOA.doc(), SECONDARY, SECONDARY_ENTRY_COUNT);
    }

    @Binds
    @BoardScope
    @PpuVar(BUS)
    MemoryBus bindPpuBus(PpuBus ppuBus); // FIXME: seems like @BoardScope on PpuBus class doesn't work as it should

    @Provides
    @BoardScope
    @PpuVar(PTS)
    static SegmentRegister providePatternTablesSegment() {
        SegmentRegister segment = new SegmentRegister(PTS.name());
        segment.setStart(PATTERN_TABLE_0_START);
        segment.setEnd(PATTERN_TABLE_1_END);

        return segment;
    }

    @Provides
    @BoardScope
    @PpuVar(LTS)
    static SegmentRegister provideLayoutTablesSegment() {
        SegmentRegister segment = new SegmentRegister(LTS.name());
        segment.setStart(LAYOUT_TABLE_0_START);
        segment.setEnd(ATTRIBUTE_TABLE_3_END); // can't exclude attribute tables 0,1,2 so let's keep the 3rd one too

        return segment;
    }

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
    @PpuVar(LT0)
    static SegmentRegister provideLayoutTable0Segment() {
        SegmentRegister segment = new SegmentRegister(LT0.name());
        segment.setStart(LAYOUT_TABLE_0_START);
        segment.setEnd(LAYOUT_TABLE_0_END);

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
}
