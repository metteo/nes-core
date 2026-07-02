package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.util.Hex;
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

    public void printAll() { // TODO: maybe draw table and sub table borders
        for(int row = 0; row < ROW_COUNT; row++) {
            for(int subRow = 0; subRow < SUBROW_COUNT; subRow++) {
                for (int col = 0; col < COL_COUNT; col++) {
                    byte attribute = table.getAttribute(row, col);
                    for(int subCol = 0; subCol < SUBCOL_COUNT; subCol++) {
                        printSub(attribute, subRow, subCol);
                    }
                }

                writer.println();
            }
        }
    }

    public void print(int row, int col) {
        for(int subRow = 0; subRow < SUBROW_COUNT; subRow++) {
            byte attribute = table.getAttribute(row, col);
            for(int subCol = 0; subCol < SUBCOL_COUNT; subCol++) {
                printSub(attribute, subRow, subCol);
            }

            writer.println();
        }
    }

    private void printSub(byte attribute, int subRow, int subCol) {
        int subAttr = subAttribute(sint(attribute), subRow, subCol);

        char c = toChar(subAttr);
        writer.print(c);
        writer.print(c);
    }

    /* package */ void printAllBytes() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COL_COUNT; col++) {
                printByte(row, col);

                writer.print(" ");
            }
            writer.println();
        }
    }

    /* package */  void printByte(int row, int col) {
        @Unsigned byte data = table.getAttribute(row, col);
        writer.print(Hex.s(data));
    }

    @Override
    public String toString() {
        return "Printer." + table.toString();
    }
}
