package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_pattern_tables">PPU pattern tables on nesdev.org</a>
 */
public class PatternTable extends Table {

    public PatternTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    /**
     * row [0x0, 0xF]
     * col [0x0, 0xF]
     */
    public String printPattern(int row, int col) {
        int index = (row << 4) | col;
        return printPattern(index);
    }

    /**
     *
     * @param index [0x00, 0xFF]
     * @return
     */
    public String printPattern(int index) {
        //      RRRR CCCC
        // ..0H NNNN NNNN Pyyy
        // ..00 0000 0000 0000 - start
        // ..01 1111 1111 1111 - end

        int half = sint(segment.getStart());
        int numb = index << 4;

        int planeLo = 0 << 3;
        int planeHi = 1 << 3;

        StringBuilder pattern = new StringBuilder();

        for (int y = 0; y < 0x8; y++) {
            int addressLo = half | numb | planeLo | y;
            int addressHi = half | numb | planeHi | y;

            @Unsigned byte byteLo = bus.access(ushort(addressLo)).read().data();
            @Unsigned byte byteHi = bus.access(ushort(addressHi)).read().data();

            for (int x = 7; x >= 0; x--) {
                int mask = 1 << x;
                int bitLo = (sint(byteLo) & mask) >> x;
                int bitHi = (sint(byteHi) & mask) >> x;

                int dot = (bitHi << 1) | bitLo;

                char c = switch(dot) {
                    case 0b11 -> '█';
                    case 0b10 -> '▓';
                    case 0b01 -> '▒';
                    case 0b00 -> '░';
                    default   -> '▞';
                };
                pattern.append(c);
            }
            pattern.append("\n");
        }

        return pattern.toString();
    }
}
