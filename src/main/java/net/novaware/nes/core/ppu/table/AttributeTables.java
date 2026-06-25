package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.register.ViewPortRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ushort;

public class AttributeTables implements Tables {

    /**
     * @see <a href="https://www.nesdev.org/wiki/PPU_scrolling#Tile_and_attribute_fetching">Attribute fetching on nesdev.org</a>
     */
    public static @Unsigned short getAttrTableAddress(ViewPortRegister viewPort) {
        int base = 0x2000; // TODO: consider using vram segment register here
        int nt = viewPort.getNameTable() << 10;
        int attr = 0b1111 << 6;
        int y = (viewPort.getCoarseY() & 0b11100) << 1;
        int x = viewPort.getCoarseX() >> 2;
        //              10   NN   1111  YYY XXX
        return ushort(base | nt | attr | y | x );
    }
}
