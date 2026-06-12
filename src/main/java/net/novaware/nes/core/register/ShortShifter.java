package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Bin;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.USHORT_MAX_VALUE;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_rendering#:~:text=shift%20registers">Shift Registers on nesdev.org</a>
 */
// TODO: maybe implement decay
public class ShortShifter extends Register {

    private @Unsigned short planeHi = USHORT_MAX_VALUE;
    private @Unsigned short planeLo = USHORT_0;

    public ShortShifter(String name) {
        super(name);
    }

    public void loadPlaneLow(@Unsigned byte lowByte) {
        planeLo = loadPlane(planeLo, lowByte);
    }

    public void loadPlaneHigh(@Unsigned byte lowByte) {
        planeHi = loadPlane(planeHi, lowByte);
    }

    private @Unsigned short loadPlane(@Unsigned short plane, @Unsigned byte lowByte) {
        int left = sint(plane) & 0xFF00;
        int right = sint(lowByte);

        int both = left | right;

        return ushort(both);
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

        int loBit = (sint(planeLo) & mask) >> shift;
        int hiBit = (sint(planeHi) & mask) >> shift;

        int bits = (hiBit << 1) | loBit;

        return ubyte(bits);
    }

    @Override
    public String toString() {
        return getName() + ".HI: " + Bin.s(planeHi) + ", " + getName() + ".LO: " + Bin.s(planeLo);
    }

    // @VisibleForTesting
    /* package */ void shiftPlanes(int numBits) {
        int ones = (0b1 << numBits) - 1;

        planeHi = ushort((sint(planeHi) << numBits) | ones);
        planeLo = ushort((sint(planeLo) << numBits));
    }

    // @VisibleForTesting
    /* package */ @Unsigned short planeLow() {
        return planeLo;
    }

    // @VisibleForTesting
    /* package */ @Unsigned short planeHigh() {
        return planeHi;
    }
}
