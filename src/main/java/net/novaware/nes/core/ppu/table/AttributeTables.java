package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.register.SegmentRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.ppu.table.AttributeTable.COL_COUNT;
import static net.novaware.nes.core.ppu.table.AttributeTable.ROW_COUNT;
import static net.novaware.nes.core.ppu.table.AttributeTable.SUBCOL_COUNT;
import static net.novaware.nes.core.ppu.table.AttributeTable.SUBROW_COUNT;
import static net.novaware.nes.core.util.Masks.BIT_1;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_attribute_tables">Attribute tables on nesdev.org</a>
 * @see <a href="https://www.nesdev.org/wiki/PPU_scrolling#Tile_and_attribute_fetching">Attribute fetching on nesdev.org</a>
 */
public class AttributeTables extends MemBusTable implements Tables {

    public static final int MEM_ROW_COUNT = 2;
    public static final int MEM_COL_COUNT = 2;
    public static final int MEM_CELL_COUNT = MEM_ROW_COUNT * MEM_COL_COUNT;

    public AttributeTables(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public @Unsigned short getAddress(ViewPortRegister viewPort) {
        int offset = segment.getStartAsInt();
        int memCell = viewPort.getLayoutTable();
        int row = viewPort.getCoarseY() >> 2;
        int col = viewPort.getCoarseX() >> 2;

        int address = getAddress(offset, memCell, row, col);
        return ushort(address);
    }

    // TODO: test when there is a base Spec for Table/s
    public @Unsigned byte getAttribute(int memRow, int memCol, int row, int col) {
        int address = getAddress(segment.getStartAsInt(), memRow, memCol, row, col);
        @Unsigned byte data = bus.access(ushort(address)).read().data();

        return data;
    }

    // region Static methods

    public static int getAddress(int offset, int memRow, int memCol, int row, int col) {
        assert 0 <= memRow && memRow < MEM_ROW_COUNT : "memRow out of range";
        assert 0 <= memCol && memCol < MEM_COL_COUNT : "memCol out of range";

        int memRowShift = memRow << 1;
        int memCell = memRowShift | memCol;

        int address = getAddress(offset, memCell, row, col);
        return address;
    }

    public static int getAddress(int offset, int memCell, int row, int col) {
        assert 0 <= offset && offset <= 0x3FFF && (offset & 0xFFF) == 0 : "offset out of range";
        assert 0 <= memCell && memCell < MEM_CELL_COUNT : "memCell out of range";
        assert 0 <= row && row < ROW_COUNT : "row out of range";
        assert 0 <= col && col < COL_COUNT : "col out of range";

        int memCellShift = memCell << 10;
        int attrShift = 0b1111 << 6;
        int segment = offset | memCellShift | attrShift;

        int address = getAddress(segment, row, col);
        return address;
    }

    public static int getAddress(int segment, int row, int col) {
        // TODO: figure out assertion for segment
        assert 0 <= row && row < ROW_COUNT : "row out of range";
        assert 0 <= col && col < COL_COUNT : "col out of range";

        int rowShift = row << 3;

        int address = segment | rowShift | col;
        return address;
    }

    /**
     * @param attribute 4 palettes in a byte
     * @return single palette for specified quadrant
     */
    public static @Unsigned byte subAttribute(@Unsigned byte attribute, ViewPortRegister viewPort) {
        int subRow = (viewPort.getCoarseY() & BIT_1) >> 1;
        int subCol = (viewPort.getCoarseX() & BIT_1) >> 1;

        return ubyte(subAttribute(sint(attribute), subRow, subCol));
    }

    public static int subAttribute(int attribute, int subRow, int subCol) {
        assert 0 <= subRow && subRow < SUBROW_COUNT : "subRow out of bounds";
        assert 0 <= subCol && subCol < SUBCOL_COUNT : "subCol out of bounds";

        int shift = subShift(subRow, subCol);
        int mask = 0b11 << shift;

        int subAttribute = (attribute & mask) >> shift;
        return subAttribute;
    }

    // endregion

    /* package */ static int subShift(int subRow, int subCol) {
        assert 0 <= subRow && subRow <= 1 : "subRow out of bounds";
        assert 0 <= subCol && subCol <= 1 : "subCol out of bounds";

        return subCol * 2 + subRow * 4;
    }

    /* package */ static int subMask(int subRow, int subCol) {
        assert 0 <= subRow && subRow <= 1 : "subRow out of bounds";
        assert 0 <= subCol && subCol <= 1 : "subCol out of bounds";

        return 0b11 << subShift(subRow, subCol);
    }
}
