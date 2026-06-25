package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.table.PatternTable
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.util.UTypes.sint

class PatternTableSpec extends Specification {

    def segment = PpuMemModule.providePatternTable0Segment()

    def "should construct an instance"() {
        given:
        MemoryBus bus = Mock()

        when:
        def instance = new PatternTable("test", segment, bus)

        then:
        instance.getName() == "test"
        instance.toString() == "test (0000:0FFF)"
    }

    def "should get first pattern from memory"() {
        given:
        PhysicalMemory patternTable0 = new PhysicalMemory("PT0",
                PATTERN_TABLE_0_START, PATTERN_TABLE_0_END, PATTERN_TABLE_0_SIZE)

        // example from https://www.nesdev.org/wiki/PPU_pattern_tables
        def pattern = [0x41, 0xC2, 0x44, 0x48, 0x10, 0x20, 0x40, 0x80, // lo plane
                       0x01, 0x02, 0x04, 0x08, 0x16, 0x21, 0x42, 0x87] // hi plane

        def bus = new TestBus(patternTable0)

        def start = sint(PATTERN_TABLE_0_START);
        for (int i = 0; i < pattern.size(); i++) {
            bus.write(start + i, pattern[i])
        }

        PatternTable patternTable = new PatternTable("PM1", segment, bus)

        when:
        def art = patternTable.printPattern(0, 0)

        then:
        art.trim() == """
             ░     █
            ░░    █ 
             ░   █  
             ░  █   
               █ ▓▓ 
              █    ▓
             █    ▓ 
            █    ▓▓▓
        """.stripIndent(12).trim()
    }
}
