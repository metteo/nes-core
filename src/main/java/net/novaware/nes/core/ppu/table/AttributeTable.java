package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_attribute_tables">Attribute Tables on nesdev.org</a>
 */
public class AttributeTable extends MemBusTable implements Table {

    public static final int ROW_COUNT = 8;
    public static final int COL_COUNT = 8;

    public static final int SUBROW_COUNT = 2;
    public static final int SUBCOL_COUNT = 2;

    public AttributeTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public @Unsigned short getAddress(int row, int col) {
        int address = AttributeTables.getAddress(segment.getStartAsInt(), row, col);

        return ushort(address);
    }

    public @Unsigned byte getAttribute(int row, int col) {
        @Unsigned short address = getAddress(row, col);
        @Unsigned byte data = bus.access(address).read().data();

        return data;
    }
}
