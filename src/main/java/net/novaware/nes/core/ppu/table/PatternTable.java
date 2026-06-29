package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.DataLine;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_pattern_tables">PPU pattern tables on nesdev.org</a>
 */
public class PatternTable extends MemBusTable implements Table {

    public PatternTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    private @Unsigned short getAddress(Pattern.Size size, int row, int col, int plane, int lineNum) {
        int baseAddress = sint(segment.getStart());
        int table = baseAddress >> 12; // TODO: doesn't look good

        int lineAddrInt = PatternTables.getAddress(size, table, row, col, plane, lineNum);

        return ushort(lineAddrInt);
    }

    // TODO: lots of params again and again. Cursor approach would make it stateful...
    public int getLine(Pattern.Size size, int row, int col, int plane, int lineNum) {
        @Unsigned short lineAddress = getAddress(size, row, col, plane, lineNum);
        @Unsigned byte line = bus.access(lineAddress).read().data();

        return sint(line);
    }

    private DataLine dataLine = new DataLine();

    public int probeLine(Pattern.Size size, int row, int col, int plane, int lineNum) {
        @Unsigned short lineAddress = getAddress(size, row, col, plane, lineNum);

        bus.probe(lineAddress, dataLine);
        @Unsigned byte line = dataLine.cycle();

        return sint(line);
    }
}
