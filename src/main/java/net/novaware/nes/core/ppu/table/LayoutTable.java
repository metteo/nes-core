package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.DataLine;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * Layout of background patterns
 * <br>
 * Nametable / NT in the docs
 *
 * @see <a href="https://www.nesdev.org/wiki/PPU_nametables">Nametables on nesdev.org</a>
 */
public class LayoutTable extends MemBusTable implements Table {

    public static final int ROW_COUNT = 30;
    public static final int COL_COUNT = 32;

    public LayoutTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public int getAddress(int row, int col) {
        int baseAddress = segment.getStartAsInt();
        // TODO: make a LayoutTables.getAddress overload that accepts segmentStart
        int offset = baseAddress & (0b11 << 12);
        int memCell = (baseAddress & (0b11 << 10)) >> 10; // FIXME: those shifts look ugly

        return LayoutTables.getAddress(offset, memCell, row, col);
    }

    /**
     * @return cell within PatternTable
     */
    public @Unsigned byte getPattern(int row, int col) {
        int address = getAddress(row, col);

        @Unsigned byte cell = bus.access(ushort(address)).read().data();
        return cell;
    }

    private DataLine probeLine = new DataLine();

    /**
     * @return cell within PatternTable
     */
    public @Unsigned byte probePattern(int row, int col) {
        int address = getAddress(row, col);

        bus.probe(ushort(address), probeLine);
        @Unsigned byte cell = probeLine.cycle();

        return cell;
    }
}
