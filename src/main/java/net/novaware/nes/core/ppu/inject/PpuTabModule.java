package net.novaware.nes.core.ppu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.memory.ObjAttrMemory;
import net.novaware.nes.core.ppu.register.ObjAttrRegister;
import net.novaware.nes.core.ppu.table.AttributeTable;
import net.novaware.nes.core.ppu.table.AttributeTables;
import net.novaware.nes.core.ppu.table.LayoutTable;
import net.novaware.nes.core.ppu.table.LayoutTables;
import net.novaware.nes.core.ppu.table.ObjAttrTable;
import net.novaware.nes.core.ppu.table.PatternTable;
import net.novaware.nes.core.ppu.table.PatternTables;
import net.novaware.nes.core.register.SegmentRegister;

import static net.novaware.nes.core.ppu.inject.PpuVarName.AT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.ATS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LT1;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LT2;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LT3;
import static net.novaware.nes.core.ppu.inject.PpuVarName.LTS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.POA;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT0;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PT1;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PTS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SOA;

@Module
public interface PpuTabModule {

    // TODO: decide if table segments should be separate or @IntoCollection

    @Provides
    @BoardScope
    @PpuVar(PTS)
    static PatternTables providePatternTables(
            @PpuVar(PTS) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new PatternTables(PTS.doc(), segment, ppuBus);
    }

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
    @PpuVar(LTS)
    static LayoutTables provideLayoutTables(
            @PpuVar(LTS) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new LayoutTables(LTS.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(LT0)
    static LayoutTable provideLayoutTable0(
            @PpuVar(LT0) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new LayoutTable(LT0.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(LT1)
    static LayoutTable provideLayoutTable1(
            @PpuVar(LT1) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new LayoutTable(LT1.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(LT2)
    static LayoutTable provideLayoutTable2(
            @PpuVar(LT2) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new LayoutTable(LT2.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(LT3)
    static LayoutTable provideLayoutTable3(
            @PpuVar(LT3) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new LayoutTable(LT3.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(ATS)
    static AttributeTables provideAttributeTables(
            @PpuVar(ATS) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new AttributeTables(ATS.doc(), segment, ppuBus);
    }

    @Provides
    @BoardScope
    @PpuVar(AT0)
    static AttributeTable provideAttributeTable0(
            @PpuVar(AT0) SegmentRegister segment,
            @PpuVar(BUS) MemoryBus ppuBus
    ) {
        return new AttributeTable(AT0.doc(), segment, ppuBus); // TODO: also 1,2,3
    }

    @Provides
    @BoardScope
    @PpuVar(POA) // TODO: consider OT0, 0T1 - ObjectTable
    static ObjAttrTable providePriObjAttrTable(
            @PpuVar(POA) ObjAttrRegister cursor,
            @PpuVar(POA) ObjAttrMemory memory
    ) {
        return new ObjAttrTable(POA.doc(), cursor, memory);
    }

    @Provides
    @BoardScope
    @PpuVar(SOA)
    static ObjAttrTable provideSecObjAttrTable(
            @PpuVar(SOA) ObjAttrRegister cursor,
            @PpuVar(SOA) ObjAttrMemory memory
    ) {
        return new ObjAttrTable(SOA.doc(), cursor, memory);
    }
}
