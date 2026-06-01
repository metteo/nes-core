package net.novaware.nes.core.file;

import net.novaware.nes.core.ppu.unit.PaletteData;

import java.net.URI;

/**
 * Palette File which is used to translate PPU color values into sRGB color space in {@link PaletteData}
 *
 * @see <a href="https://www.nesdev.org/wiki/.pal">.pal file format</a>
 */
public record PalFile(
    URI origin
    // TODO: meta
    // TODO: data
    // hash not needed
) {

}
