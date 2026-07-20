package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Bin;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.USHORT_MASK;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_rendering#:~:text=shift%20registers">Shift Registers on nesdev.org</a>
 */
// TODO: maybe implement decay
public class ShortShifter extends Register {

    private static final int MASK = USHORT_MASK;

    private int planeHi = MASK;
    private int planeLo = 0;

    public ShortShifter(String name) {
        super(name);
    }

    public void loadPlaneLow(@Unsigned byte lowByte) {
        planeLo = loadPlane(planeLo, sint(lowByte));
    }

    public void loadPlaneHigh(@Unsigned byte lowByte) {
        planeHi = loadPlane(planeHi, sint(lowByte));
    }

    private static int loadPlane(int plane, int lowByte) {
        int left = plane & 0xFF00;
        int right = lowByte;

        int both = (left | right) & MASK;

        return both;
    }

    public void shiftPlanes() {
        // TODO: maybe optimize by doing: "value = (value << 1) | 1;" without creating a 1s mask
        shiftPlanes(1);
    }

    /**
     *
     * @param offset fineX usually
     * @return palette num or palette offset
     */
    public @Unsigned byte getBits(int offset) {
        assert 0 <= offset && offset <= 7 : "offset out of range";

        int shift = 0xF - offset;
        int mask = 0b1 << shift;

        int loBit = (planeLo & mask) >> shift;
        int hiBit = (planeHi & mask) >> shift;

        int bits = (hiBit << 1) | loBit;

        return ubyte(bits);
    }

    @Override
    public String toString() {
        return getName() + ".HI: " + Bin.s(ushort(planeHi)) + ", " + getName() + ".LO: " + Bin.s(ushort(planeLo));
    }

    // @VisibleForTesting
    /* package */ void shiftPlanes(int numBits) {
        int ones = (0b1 << numBits) - 1;

        planeHi = ((planeHi << numBits) | ones) & MASK;
        planeLo = ((planeLo << numBits)) & MASK;
    }

    // @VisibleForTesting
    /* package */ @Unsigned short planeLow() {
        return ushort(planeLo);
    }

    // @VisibleForTesting
    /* package */ @Unsigned short planeHigh() {
        return ushort(planeHi);
    }
}
