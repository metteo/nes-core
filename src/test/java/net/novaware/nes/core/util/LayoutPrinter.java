package net.novaware.nes.core.util;

import net.novaware.nes.core.ppu.table.LayoutTable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.io.PrintWriter;

import static net.novaware.nes.core.ppu.table.LayoutTable.COL_COUNT;
import static net.novaware.nes.core.ppu.table.LayoutTable.ROW_COUNT;

public class LayoutPrinter {

    private final LayoutTable layoutTable;
    private final PrintWriter writer;

    public LayoutPrinter(LayoutTable layoutTable, PrintWriter writer) {
        this.layoutTable = layoutTable;
        this.writer = writer;
    }

    public void printAll() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COL_COUNT; col++) {
                print(row, col);

                writer.print(" ");
            }
            writer.println();
        }
    }

    public void print(int row, int col) {
        @Unsigned byte pattern = layoutTable.probePattern(row, col);
        writer.print(Hex.s(pattern));
    }
}
