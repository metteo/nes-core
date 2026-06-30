package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.SegmentRegister;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * Layout of background patterns
 * <br>
 * Nametable / NT in the docs
 *
 * @see <a href="https://www.nesdev.org/wiki/PPU_nametables">Name Tables on nesdev.org</a>
 */
public class LayoutTable extends MemBusTable implements Table {

    private static final int ROW_COUNT = 30;
    private static final int COL_COUNT = 32;

    public LayoutTable(String name, SegmentRegister segment, MemoryBus bus) {
        super(name, segment, bus);
    }

    public String printBackground() {
        StringBuilder background = new StringBuilder();

        for (int y = 0; y < ROW_COUNT; y++) {
            for (int x = 0; x < COL_COUNT; x++) {
                background.append(Hex.s(getBackground(y, x))).append(" ");
            }
            background.append("\n");
        }

        return background.toString();
    }

    public @Unsigned byte getBackground(int y, int x) {
        int start = segment.getStartAsInt();
        int address = start + (COL_COUNT * y) + x;

        return bus.access(ushort(address)).read().data();
    }
}
