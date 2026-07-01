package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.register.SegmentRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.ppu.table.LayoutTable.COL_COUNT;
import static net.novaware.nes.core.ppu.table.LayoutTable.ROW_COUNT;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * Nametables / NTs in the docs
 *
 * @see <a href="https://www.nesdev.org/wiki/PPU_nametables">Nametables on nesdev.org</a>
 */
public class LayoutTables extends MemBusTable implements Tables {

    public static final int MEM_ROW_COUNT = 2;
    public static final int MEM_COL_COUNT = 2;
    public static final int MEM_CELL_COUNT = MEM_ROW_COUNT * MEM_COL_COUNT;

    public LayoutTables(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public static @Unsigned short getAddress(ViewPortRegister viewPort) {
        int offset = 0x2000; // TODO: consider using vram segment register here

        int address = getAddress(
            offset,
            viewPort.getLayoutTable(),
            viewPort.getCoarseY(),
            viewPort.getCoarseX()
        );

        return ushort(address);
    }

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
        assert 0 <= row && row < ROW_COUNT + 2 : "row out of range"; // some programs may read from attribute area
        assert 0 <= col && col < COL_COUNT : "col out of range";

        int memCellShift = memCell << 10;
        int rowShift = row << 5;

        int address = offset | memCellShift | rowShift | col;

        return address;
    }

    public static int getAddress(int segment, int row, int col) {
        // TODO: implement
        return 0;
    }
}
