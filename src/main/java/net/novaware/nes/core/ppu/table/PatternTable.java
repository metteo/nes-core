package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.DataLine;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.ppu.table.Pattern.Size.SINGLE;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_pattern_tables">PPU pattern tables on nesdev.org</a>
 */
public class PatternTable extends MemBusTable implements Table {

    public PatternTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    /**
     * row [0x0, 0xF]
     * col [0x0, 0xF]
     */
    public String printPattern(Pattern.Size size, int row, int col) {
        int index = (row << 4) | col;
        return printPattern(size, index);
    }

    public void dump() { // TODO: dump should draw the table instead of tile under tile
        for (int i = 0x00; i < 0x100; i++) {
            String pattern = printPattern(SINGLE, i);
            System.out.println(Hex.s(ubyte(i)));
            System.out.println(pattern);
        }
    }

    /**
     *
     * @param cell [0x00, 0xFF]
     * @return
     */
    public String printPattern(Pattern.Size size, int cell) {

        DataLine dataLine = new DataLine();

        int baseAddress = sint(segment.getStart());
        int table = baseAddress >> 12;

        StringBuilder pattern = new StringBuilder();

        pattern.append("╔═"); // TODO: formatting / conversion to chars should be a separate util
        for(int x = 0; x < 8; x++) {
            pattern.append("══");
        }
        pattern.append("═╗\n");

        for (int y = 0; y < size.height(); y++) {
            int addressLo = PatternTables.getAddress(size, table, cell, 0, y);
            int addressHi = PatternTables.getAddress(size, table, cell, 1, y);

            bus.probe(ushort(addressLo), dataLine);
            @Unsigned byte byteLo = dataLine.cycle();

            bus.probe(ushort(addressHi), dataLine);
            @Unsigned byte byteHi = dataLine.cycle();

            pattern.append("║ ");

            for (int x = size.width() - 1; x >= 0; x--) {
                int mask = 1 << x;
                int bitLo = (sint(byteLo) & mask) >> x;
                int bitHi = (sint(byteHi) & mask) >> x;

                int dot = (bitHi << 1) | bitLo;

                char c = switch(dot) {
                    case 0b11 -> '█';
                    case 0b10 -> '▓';
                    case 0b01 -> '░';
                    case 0b00 -> ' ';
                    default   -> '▒'; // error
                };
                pattern.append(c).append(c);
            }
            pattern.append(" ║\n");
        }

        pattern.append("╚═");
        for(int x = 0; x < 8; x++) {
            pattern.append("══");
        }
        pattern.append("═╝\n");

        return pattern.toString();
    }
}
