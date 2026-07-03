package net.novaware.nes.core.ppu.unit;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.awt.*;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static net.novaware.nes.core.util.Asserts.assertNonNull;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * @see net.novaware.nes.core.file.PalFile
 */
public class PaletteData {

    // TODO: figure out if this is the best way
    public static final @Unsigned byte COLOR_TRANSPARENT = ubyte(0x40);

    /**
     * @author gemini: Java NES Palette Colors Array
     */
    private static final int[] COLORS = {
            // Row 0: Grays and dark saturated colors
            0x7C7C7C, 0x0000FC, 0x0000BC, 0x4428BC, 0x940084, 0xA80020, 0xA81000, 0x881400,
            0x503000, 0x007800, 0x006800, 0x005800, 0x004058, 0x000000, 0x000000, 0x000000,

            // Row 1: Medium tones and primary colors
            0xBCBCBC, 0x0078F8, 0x0058F8, 0x6844FC, 0xD800CC, 0xE40058, 0xF83800, 0xE45C10,
            0xAC7C00, 0x00B800, 0x00A800, 0x00A844, 0x008888, 0x000000, 0x000000, 0x000000,

            // Row 2: Bright tones and pastels
            0xF8F8F8, 0x3CBCFC, 0x6888FC, 0x9878FC, 0xF878F8, 0xF85898, 0xF87858, 0xFCA044,
            0xF8B800, 0xB8F818, 0x58D854, 0x58F898, 0x00E8D8, 0x787878, 0x000000, 0x000000,

            // Row 3: Very bright, washed-out tones
            0xF8F8F8, 0xA4E4FC, 0xB8B8FC, 0xD8B8FC, 0xF8B8FC, 0xF8A4C0, 0xF0D0B0, 0xFCE4A0,
            0xF8E870, 0xD8F878, 0xB8F8B8, 0xB8F8D8, 0x00FCFC, 0xD8D8D8, 0x000000, 0x000000
    };

    private static final Color[] COLOR_OBJECTS; // FIXME cache for now, but this is AWT in core code
    private static final Map<Color, Color> COLOR_OBJECTS_DARKER;

    static {
        COLOR_OBJECTS = IntStream.of(COLORS)
                .mapToObj(Color::new)
                .toArray(Color[]::new);

        COLOR_OBJECTS_DARKER = Stream.of(COLOR_OBJECTS)
                .distinct()
                .collect(toMap(identity(), Color::darker));
    }

    public int getColor(@Unsigned byte index) {
        return COLORS[sint(index)];
    }

    public Color getColorObj(@Unsigned byte index) {
        return COLOR_OBJECTS[sint(index)];
    }

    public static Color getDarker(Color color) {
        return assertNonNull(COLOR_OBJECTS_DARKER.get(color), "darker color is null");
    }
}
