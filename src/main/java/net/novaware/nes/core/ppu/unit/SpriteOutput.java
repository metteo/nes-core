package net.novaware.nes.core.ppu.unit;

import net.novaware.nes.core.ppu.table.LayoutTable;
import net.novaware.nes.core.register.ByteShifter;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.Arrays;

import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

// TODO: refactor this into a branchless impl, without using external refs like integer counter or ByteShifter
// TODO: consider data oriented. One instance having arrays for fields where every index is a separate unit
// TODO: consider packing multiple sprites bits into a byte. 2 sparse arrays of 256 bytes could contain a full line of 8 sprites
//  no shifting, no counters. just extract the first non 0 bits for muxing
//  having more than 2x or 4x amount of output units would require short/int, or additional output units, to be decided
public class SpriteOutput {

    public static final int COL_COUNT = LayoutTable.COL_COUNT; // TODO: unify this constant and move somewhere

    private final @Unsigned byte[] patternHi;
    private final @Unsigned byte[] patternLo;

    private final @Unsigned byte[] paletteHi;
    private final @Unsigned byte[] paletteLo;

    private final @Unsigned byte[] priority;

    public SpriteOutput() {
        patternHi = new byte[COL_COUNT];
        patternLo = new byte[COL_COUNT];

        paletteHi = new byte[COL_COUNT];
        paletteLo = new byte[COL_COUNT];

        priority = new byte[COL_COUNT];
    }

    // TODO: handle cases when the sprite hangs off the right side and should wrap into left side
    // but only with horizontal mirroring like Mario Bros or Ice Climber, Wrecking Crew
    public static void loadByte(@Unsigned byte[] dest, int x, int val) { // FIXME: overrides previous loads, sprite priority is reversed
        int coarseX = (x >> 3) & 0b11111;
        int fineX = x & 0b111;

        if (fineX == 0) {
            dest[coarseX] = ubyte(val);
        } else { // split
            int fineXMask = (0xFF << (8 - fineX)) & 0xFF; // right input
            int remainXMask = (~fineXMask) & 0xFF;        // left  input

            int leftPriInt = remainXMask & val >> fineX;
            int rightPriInt = fineXMask & val << (8 - fineX);

            int leftIndex = coarseX;
            int rightIndex = (coarseX + 1) & (COL_COUNT - 1); // wrapping should be optional

            int leftByte = sint(dest[leftIndex]);
            int rightByte = sint(dest[rightIndex]);

            int newLeftByte = (fineXMask & leftByte) | leftPriInt;
            int newRightByte = rightPriInt | (remainXMask & rightByte);

            dest[leftIndex] = ubyte(newLeftByte);
            dest[rightIndex] = ubyte(newRightByte);
        }
    }

    private static int getBit(@Unsigned byte[] src, int coarseX, int fineX) {
        int fineXShift = 7 - fineX;

        int line = sint(src[coarseX]);
        int bit = (line & (1 << fineXShift)) >> fineXShift;

        return bit & 0b1;
    }

    public void loadPriority(int x, @Unsigned byte priority) {
        int priInt = sint(priority) * 0xFF;

        loadByte(this.priority, x, priInt);
    }

    public void loadPalette(int x, @Unsigned byte palette) {
        int palInt = sint(palette);

        int paletteHi = ((palInt & 0b10) >> 1) * 0xFF;
        int paletteLo =  (palInt & 0b01)       * 0xFF;

        loadByte(this.paletteHi, x, paletteHi);
        loadByte(this.paletteLo, x, paletteLo);
    }

    public void loadPatternHi(int x, @Unsigned byte patternHi) {
        loadByte(this.patternHi, x, sint(patternHi));
    }

    public void loadPatternLo(int x, @Unsigned byte patternLo) {
        loadByte(this.patternLo, x, sint(patternLo));
    }

    public @Unsigned byte getPattern(int x) {
        int coarseX = (x >> 3) & 0b11111;
        int fineX = x & 0b111;

        int bitHi = getBit(patternHi, coarseX, fineX) << 1;
        int bitLo = getBit(patternLo, coarseX, fineX);

        return ubyte(bitHi | bitLo);
    }

    public @Unsigned byte getPalette(int x) {
        assert 0 <= x && x <= 0xFF : "x out of bounds";
        int coarseX = (x >> 3) & 0b11111;
        int fineX = x & 0b111;

        int bitHi = getBit(paletteHi, coarseX, fineX) << 1;
        int bitLo = getBit(paletteLo, coarseX, fineX);

        return ubyte(bitHi | bitLo);
    }

    public @Unsigned byte getPriority(int x) {
        int coarseX = (x >> 3) & 0b11111;
        int fineX = x & 0b111;

        int bit = getBit(priority, coarseX, fineX);

        return ubyte(bit);
    }

    public void clear() {
        Arrays.fill(patternHi, UBYTE_0);
        Arrays.fill(patternLo, UBYTE_0);

        Arrays.fill(paletteHi, UBYTE_0);
        Arrays.fill(paletteLo, UBYTE_0);

        Arrays.fill(priority, UBYTE_0);
    }


    // region old stuff for removal

    public ByteShifter shifter = new ByteShifter("SPOU?");

    public @Unsigned byte palette;

    public boolean hidden;

    public int countDown; // [0, x] waiting

    public int xCounter;

    public boolean active;


    // FIXME: this method takes a lot of cpu time
    public void maybeShiftPlanes() {
        if (countDown > 0) {
            countDown--;
            return;
        }

        if (xCounter > 0) {
            xCounter--;
            shifter.shiftPlanes();
        }
    }

    public boolean shouldDraw() {
        return active & countDown == 0 && xCounter > 0;
    }

    // endregion
}
