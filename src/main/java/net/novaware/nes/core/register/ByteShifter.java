package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Bin;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class ByteShifter extends Register {

    private @Unsigned byte planeHi = UBYTE_MAX_VALUE;
    private @Unsigned byte planeLo = UBYTE_0;

    public ByteShifter(String name) {
        super(name);
    }

    public void loadPlaneLow(@Unsigned byte lowByte) {
        planeLo = lowByte;
    }

    public void loadPlaneHigh(@Unsigned byte lowByte) {
        planeHi = lowByte;
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

        int shift = 0x7 - offset;
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

        planeHi = ubyte((sint(planeHi) << numBits) | ones);
        planeLo = ubyte((sint(planeLo) << numBits));
    }

    // @VisibleForTesting
    /* package */ @Unsigned byte planeLow() {
        return planeLo;
    }

    // @VisibleForTesting
    /* package */ @Unsigned byte planeHigh() {
        return planeHi;
    }
}
