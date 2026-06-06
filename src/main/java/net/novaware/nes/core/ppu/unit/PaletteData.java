package net.novaware.nes.core.ppu.unit;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * @see net.novaware.nes.core.file.PalFile
 */
public class PaletteData {

    // TODO: figure out if this is the best way
    public static final @Unsigned byte COLOR_TRANSPARENT = ubyte(0x40);

    public int getColor(@Unsigned byte index) {
        return 0;
    }
}
