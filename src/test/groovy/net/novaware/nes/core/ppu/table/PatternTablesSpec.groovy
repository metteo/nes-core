package net.novaware.nes.core.ppu.table

import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.ppu.table.Pattern.Size.DOUBLE
import static net.novaware.nes.core.ppu.table.Pattern.Size.SINGLE

class PatternTablesSpec extends Specification {

    def segment = PpuMemModule.providePatternTablesSegment()

    def memory = new PhysicalMemory("PTS", PATTERN_TABLE_0_START, PATTERN_TABLE_1_END,
            PATTERN_TABLE_0_SIZE + PATTERN_TABLE_1_SIZE)

    def ppuBus = new TestBus(memory)

    def "should construct an instance"() {
        when:
        def instance = new PatternTables("PTs", segment, ppuBus)

        then:
        instance.getName() == "PTs"
        instance.toString() == "PTs (0000:1FFF)"
    }

    def "should construct single pattern address"() {
        when:
        def address = PatternTables.getAddress(SINGLE, tab, row, col, plane, line)

        then:
        address == outAddress

        where:
        tab | row    | col    | plane | line   || outAddress
        //  |   rrrr |   cccc |       |        ||       rrrr_cccc
        //  |        |        |       |        ||   0_T_CCCC_CCCC_P_LLL
        0b0 | 0b0000 | 0b0000 |   0b0 |  0b000 || 0b0_0_0000_0000_0_000
        0b0 | 0b0000 | 0b0000 |   0b0 |  0b111 || 0b0_0_0000_0000_0_111
        0b0 | 0b0000 | 0b0000 |   0b1 |  0b000 || 0b0_0_0000_0000_1_000
        0b0 | 0b0000 | 0b1111 |   0b0 |  0b000 || 0b0_0_0000_1111_0_000
        0b0 | 0b1111 | 0b0000 |   0b0 |  0b000 || 0b0_0_1111_0000_0_000
        0b1 | 0b0000 | 0b0000 |   0b0 |  0b000 || 0b0_1_0000_0000_0_000
        0b1 | 0b1111 | 0b1111 |   0b1 |  0b111 || 0b0_1_1111_1111_1_111
    }

    def "should construct double pattern address"() {
        when:
        def address = PatternTables.getAddress(DOUBLE, tab, row, col, plane, line)

        then:
        address == outAddress

        where:
        tab | row    | col   | half | plane | line     || outAddress | comment
        //  |   rrrr |   ccc |      |       |   h_lll  ||       rrrr_ccc_h_._lll |
        //T |   CCCC |   CCC |      |     P |   L_LLL  ||   0_T_CCCC_CCC_L_P_LLL |
        0b0 | 0b0000 | 0b000 | _    |   0b0 | 0b0_000  || 0b0_0_0000_000_0_0_000 | "zeroes"
        0b0 | 0b0000 | 0b000 | _    |   0b0 | 0b0_111  || 0b0_0_0000_000_0_0_111 | "line 0-7"
        0b0 | 0b0000 | 0b000 | _    |   0b0 | 0b1_000  || 0b0_0_0000_000_1_0_000 | "line 8-15"
        0b0 | 0b0000 | 0b000 | _    |   0b1 | 0b0_000  || 0b0_0_0000_000_0_1_000 | "plane"
        0b0 | 0b0000 | 0b111 | _    |   0b0 | 0b0_000  || 0b0_0_0000_111_0_0_000 | "column"
        0b0 | 0b1111 | 0b000 | _    |   0b0 | 0b0_000  || 0b0_0_1111_000_0_0_000 | "row"
        0b1 | 0b0000 | 0b000 | _    |   0b0 | 0b0_000  || 0b0_1_0000_000_0_0_000 | "table"
        0b0 | 0b0000 | 0b000 | _    |   0b0 | 0b1_111  || 0b0_0_0000_000_1_0_111 | "line 0-15"
        0b1 | 0b1111 | 0b111 | _    |   0b1 | 0b1_111  || 0b0_1_1111_111_1_1_111 | "ones"
    }
}
