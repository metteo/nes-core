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

    private DataLine dataLine = new DataLine();

    public PatternTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public int getLine(Pattern.Size size, int row, int col, int plane, int lineNum) {
        int baseAddress = sint(segment.getStart());
        int table = baseAddress >> 12; // TODO: doesn't look good

        int lineAddr = PatternTables.getAddress(size, table, row, col, plane, lineNum);

        // TODO: allow switching between main access and probe
        // TODO: maybe create wrapper MemoryBus that delegates to probe?
        //@Unsigned byte line = bus.access(ushort(lineAddr)).read().data();

        bus.probe(ushort(lineAddr), dataLine);
        @Unsigned byte line = dataLine.cycle();

        return sint(line);
    }
}
