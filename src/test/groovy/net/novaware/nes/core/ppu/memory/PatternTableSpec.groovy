package net.novaware.nes.core.ppu.memory

import net.novaware.nes.core.memory.MemoryBus
import net.novaware.nes.core.memory.PhysicalMemory
import net.novaware.nes.core.ppu.inject.PpuMemModule
import net.novaware.nes.core.ppu.table.PatternTable
import net.novaware.nes.core.ppu.table.PatternPrinter
import net.novaware.nes.core.test.TestBus
import spock.lang.Specification

import static net.novaware.nes.core.ppu.memory.PpuMemMap.*
import static net.novaware.nes.core.ppu.table.Pattern.Size.DOUBLE
import static net.novaware.nes.core.ppu.table.Pattern.Size.SINGLE
import static net.novaware.nes.core.util.UTypes.sint

class PatternTableSpec extends Specification {

    def segment = PpuMemModule.providePatternTable0Segment()

    def stringWriter = new StringWriter()
    def printWriter = new PrintWriter(stringWriter)

    // example from https://www.nesdev.org/wiki/PPU_pattern_tables
    static def squareData = [0x41, 0xC2, 0x44, 0x48, 0x10, 0x20, 0x40, 0x80, // lo plane
                             0x01, 0x02, 0x04, 0x08, 0x16, 0x21, 0x42, 0x87] // hi plane

    static def squareChars = """
        ╔══════════════════╗
        ║   ░░          ██ ║
        ║ ░░░░        ██   ║
        ║   ░░      ██     ║
        ║   ░░    ██       ║
        ║       ██  ▓▓▓▓   ║
        ║     ██        ▓▓ ║
        ║   ██        ▓▓   ║
        ║ ██        ▓▓▓▓▓▓ ║
        ╚══════════════════╝
    """.stripIndent(8).trim()

    static def tallData = [
        // Top
        0x40, 0xC0, 0x40, 0x40, 0x41, 0xE2, 0x04, 0x08, // Low
        0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x04, 0x08, // High

        // Bottom
        0x10, 0x20, 0x40, 0x80, 0x00, 0x00, 0x00, 0x00, // Low
        0x10, 0x20, 0x46, 0x89, 0x01, 0x02, 0x04, 0x0F  // High
    ]

    static def tallChars = """
        ╔══════════════════╗
        ║   ░░             ║
        ║ ░░░░             ║
        ║   ░░             ║
        ║   ░░             ║
        ║   ░░          ██ ║
        ║ ░░░░░░      ██   ║
        ║           ██     ║
        ║         ██       ║
        ║       ██         ║
        ║     ██           ║
        ║   ██      ▓▓▓▓   ║
        ║ ██      ▓▓    ▓▓ ║
        ║               ▓▓ ║
        ║             ▓▓   ║
        ║           ▓▓     ║
        ║         ▓▓▓▓▓▓▓▓ ║
        ╚══════════════════╝
    """.stripIndent(8).trim()

    def "should construct an instance"() {
        given:
        MemoryBus bus = Mock()

        when:
        def instance = new PatternTable("test", segment, bus)

        then:
        instance.getName() == "test"
        instance.toString() == "test (0000:0FFF)"
    }

    def "should get first #shape pattern from memory"() {
        given:
        def patternTable0 = new PhysicalMemory("PT0",
                PATTERN_TABLE_0_START, PATTERN_TABLE_0_END, PATTERN_TABLE_0_SIZE)

        def bus = new TestBus(patternTable0)

        def start = sint(PATTERN_TABLE_0_START);
        for (int i = 0; i < data.size(); i++) {
            bus.write(start + i, data[i])
        }

        def patternTable = new PatternTable("PM1", segment, bus)
        def patternPrinter = new PatternPrinter(patternTable, printWriter)

        when:
        patternPrinter.print(size, 0, 0)
        //patternPrinter.printAll()
        def art = stringWriter.toString()

        then:
        //println art
        art.trim() == chars

        where:
        size   | data       || chars       | shape
        SINGLE | squareData || squareChars | "square"
        DOUBLE | tallData   || tallChars   | "tall"
    }
}
