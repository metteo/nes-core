package net.novaware.nes.core.util;

import net.novaware.nes.core.ppu.table.AttributeTable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.io.PrintWriter;

import static net.novaware.nes.core.ppu.table.AttributeTable.COL_COUNT;
import static net.novaware.nes.core.ppu.table.AttributeTable.ROW_COUNT;
import static net.novaware.nes.core.ppu.table.AttributeTable.SUBCOL_COUNT;
import static net.novaware.nes.core.ppu.table.AttributeTable.SUBROW_COUNT;
import static net.novaware.nes.core.ppu.table.AttributeTables.subAttribute;
import static net.novaware.nes.core.util.Blocks.toChar;
import static net.novaware.nes.core.util.UTypes.sint;

public class AttributePrinter {

    private final AttributeTable table;
    private final PrintWriter writer;

    public AttributePrinter(AttributeTable table, PrintWriter writer) {
        this.table = table;
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
        @Unsigned byte data = table.getAttribute(row, col);
        writer.print(Hex.s(data));
    }

    public void printAll2() { // TODO: draw table and sub table borders
        for(int row = 0; row < ROW_COUNT; row++) {
            for(int subRow = 0; subRow < SUBROW_COUNT; subRow++) {
                for (int col = 0; col < COL_COUNT; col++) {
                    byte attribute = table.getAttribute(row, col);
                    for(int subCol = 0; subCol < SUBCOL_COUNT; subCol++) {
                        int subAttr = subAttribute(sint(attribute), subRow, subCol);

                        char c = toChar(subAttr);
                        writer.print(c);
                        writer.print(c);
                    }
                }

                writer.println();
            }
        }
    }
}
