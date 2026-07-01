package net.novaware.nes.core.util;

import net.novaware.nes.core.ppu.table.Pattern;
import net.novaware.nes.core.ppu.table.PatternTable;

import java.io.PrintWriter;

import static net.novaware.nes.core.ppu.table.Pattern.Size.SINGLE;
import static net.novaware.nes.core.util.Blocks.toChar;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Box-drawing_characters">Box drawing</a>
 */
public class PatternPrinter {

    private final PatternTable table;
    private final PrintWriter writer;

    public PatternPrinter(PatternTable table, PrintWriter writer) {
        this.table = table;
        this.writer = writer;
    }

    public void printAll() {
        final Pattern.Size size = SINGLE;
        final int rows = 0x10;
        final int cols = 0x10;

        printTopBorder(size, cols);

        for (int r = 0x0; r < rows; r++) {
            for(int l = 0; l < size.lines(); l++) {

                printLeftBorder();

                for (int c = 0x0; c < cols; c++) {
                    printLine(size, r, c, l);

                    if (c < cols - 1) {
                        printColumnSeparator();
                    }
                }
                printRightBorder();

                writer.println();
            }
            if (r < rows - 1) {
                printRowSeparator(size, cols);
            }
        }
        printBottomBorder(size, cols);
    }

    /**
     *
     * @param row [0x0, 0xF]
     * @param col [0x0, 0xF] for single, [0x0, 0x7] for double
     */
    public void print(Pattern.Size size, int row, int col) {
        printTopBorder(size, 1);

        for (int l = 0; l < size.lines(); l++) {
            printLeftBorder();
            printLine(size, row, col, l);
            printRightBorder();
            writer.println();
        }

        printBottomBorder(size, 1);
    }

    private void printLine(Pattern.Size size, int row, int col, int l) {
        int byteLo = table.probeLine(size, row, col, 0, l);
        int byteHi = table.probeLine(size, row, col, 1, l);

        for (int d = size.dots() - 1; d >= 0; d--) {
            int mask = 1 << d;
            int bitLo = (byteLo & mask) >> d;
            int bitHi = (byteHi & mask) >> d;

            int dot = (bitHi << 1) | bitLo;

            char c = toChar(dot);

            writer.print(c);
            writer.print(c);
        }
    }

    private void printTopBorder(Pattern.Size size, int cols) {
        printHorizontalLine(size, cols, "╔═", "══", "═╤═", "═╗");
    }

    private void printLeftBorder() {
        writer.print("║ ");
    }

    private void printColumnSeparator() {
        writer.print(" │ ");
    }

    private void printRightBorder() {
        writer.print(" ║");
    }

    private void printRowSeparator(Pattern.Size size, int cols) {
        printHorizontalLine(size, cols, "╟─", "──", "─┼─", "─╢");
    }

    private void printBottomBorder(Pattern.Size size, int cols) {
        printHorizontalLine(size, cols, "╚═", "══", "═╧═", "═╝");
    }

    private void printHorizontalLine(
        Pattern.Size size,
        int cols,
        String start,
        String middle,
        String colSep,
        String end
    ) {
        writer.print(start);
        for(int c = 0; c < cols; c++) {
            for(int d = 0; d < size.dots(); d++) {
                writer.print(middle);
            }

            if (c < cols - 1) {
                writer.print(colSep);
            }
        }
        writer.print(end);
        writer.println();
    }
}
