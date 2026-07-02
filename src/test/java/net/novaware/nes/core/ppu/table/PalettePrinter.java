package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.table.PaletteTable.Layer;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.io.PrintWriter;

public class PalettePrinter {

    private final PaletteTable table;
    private final PrintWriter writer;

    public PalettePrinter(PaletteTable table, PrintWriter writer) {
        this.table = table;
        this.writer = writer;
    }

    public void printAll() {
        Layer[] layers = Layer.values();

        for (Layer layer : layers) {
            writer.print(layer.name());
            writer.print("\t");

            for (int p = 0; p < 4; p++) {
                for (int o = 0; o < 4; o++) {
                    @Unsigned byte colorRef = table.getColorRef(layer, p, o);

                    writer.print(Hex.s(colorRef));
                    writer.print(" ");
                }
                writer.print("\t");
            }
            writer.println();
        }
    }
}
