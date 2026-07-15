package net.novaware.nes.core.ppu.unit;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.Arrays;

import static net.novaware.nes.core.config.VideoStandard.ACTIVE_WIDTH;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

// TODO: refactor this into a branchless impl,
//  no shifting, no counters. just extract the first non 0 bits for muxing
public class SpriteOutput {

    private static final int MASK_PATTERN  = 0b0000_0011;
    private static final int MASK_PALETTE  = 0b0000_1100;
    private static final int MASK_HIDDEN   = 0b0001_0000;
    private static final int MASK_SPRITE_0 = 0b0010_0000;
    private static final int MASK_OVERFLOW = 0b0100_0000; // over 8 sprite limit
    private static final int MASK_DIRTY    = 0b1000_0000; // already has some sprite

    private final @Unsigned byte[] dots;

    // TODO: have staging area for assembling sprite data, loop through 8 bit slots only on commit
    // TODO: staging area here or as a separate object / register?
    public int x2;
    public @Unsigned byte patternLo2;
    public @Unsigned byte patternHi2;
    public int palette2;
    public boolean hidden2;
    public int number2;

    // TODO: reading api should cache dot and allow fast access to attributes and pal/pat

    public SpriteOutput() {
        dots = new byte[ACTIVE_WIDTH];
    }

    public void loadLine(
        // TODO: consider making these a reusable object
        @Unsigned byte x,
        @Unsigned byte patternHi,
        @Unsigned byte patternLo,
        int palette,
        boolean hidden,
        int number
    ) {
        int intX = sint(x);
        int patHi = sint(patternHi);
        int patLo = sint(patternLo);

        int pal = (palette & 0b11) << 2;
        int hid = hidden ? MASK_HIDDEN : 0;
        int sov = number > 0x7 ? MASK_OVERFLOW : 0;
        int s0 = number == 0 ? MASK_SPRITE_0 : 0;

        int partialDot = MASK_DIRTY | sov | s0 | hid | pal;

        for (int bit = 0, index, shift; bit < 8; ++bit) {
            // TODO: handle cases when the sprite hangs off the right side and should wrap into left side
            // but only with horizontal mirroring like Mario Bros or Ice Climber, Wrecking Crew
            index = (intX + bit) & 0xFF; // TODO: make wrapping of sprites configurable, default to clipping

            @Unsigned byte prevDot = dots[index];
            if (isDirty(prevDot)) {
                continue;
            }

            shift = 7 - bit;
            int patHiBit = (patHi >> shift) & 0b1;
            int patLoBit = (patLo >> shift) & 0b1;
            int pattern = patHiBit << 1 | patLoBit;

            int newDot = partialDot | pattern;
            int finalDot = pattern != 0 ? newDot : 0;

            dots[index] = ubyte(finalDot);
        }
    }

    public void clear() {
        Arrays.fill(dots, UBYTE_0);
    }

    public void commit() {
        loadLine(ubyte(x2), patternHi2, patternLo2, palette2, hidden2, number2);
    }

    public @Unsigned byte getDot(@Unsigned byte x) {
        return dots[sint(x)];
    }

    public static @Unsigned byte asPattern(@Unsigned byte dot) {
        return ubyte(sint(dot) & MASK_PATTERN);
    }

    public static @Unsigned byte asPalette(@Unsigned byte dot) {
        return ubyte((sint(dot) & MASK_PALETTE) >> 2);
    }

    public static boolean isHidden(@Unsigned byte dot) {
        return (sint(dot) & MASK_HIDDEN) != 0;
    }

    public static boolean isSprite0(@Unsigned byte dot) {
        return (sint(dot) & MASK_SPRITE_0) != 0;
    }

    public static boolean isOverflow(@Unsigned byte dot) {
        return (sint(dot) & MASK_OVERFLOW) != 0;
    }

    public static boolean isDirty(@Unsigned byte dot) {
        return (sint(dot) & MASK_DIRTY) != 0;
    }
}
