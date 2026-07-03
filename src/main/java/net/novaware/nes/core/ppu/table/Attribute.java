package net.novaware.nes.core.ppu.table;

import net.novaware.nes.core.ppu.register.ViewPortRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Masks.BIT_1;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class Attribute {

    public static final int SUBROW_COUNT = 2;
    public static final int SUBCOL_COUNT = 2;

    /**
     * @param attribute 4 palette rows in a byte
     * @return single palette row for specified quadrant
     */
    public static @Unsigned byte asPalette(@Unsigned byte attribute, ViewPortRegister viewPort) {
        int subRow = (viewPort.getCoarseY() & BIT_1) >> 1;
        int subCol = (viewPort.getCoarseX() & BIT_1) >> 1;

        return ubyte(asPalette(sint(attribute), subRow, subCol));
    }

    public static int asPalette(int attribute, int subRow, int subCol) {
        assertRowAndCol(subRow, subCol);

        int shift = subShift(subRow, subCol);
        int mask = 0b11 << shift;

        int subAttribute = (attribute & mask) >> shift;
        return subAttribute;
    }

    private static void assertRowAndCol(int subRow, int subCol) {
        assert 0 <= subRow && subRow < SUBROW_COUNT : "subRow out of bounds";
        assert 0 <= subCol && subCol < SUBCOL_COUNT : "subCol out of bounds";
    }

    // endregion

    /* package */ static int subShift(int subRow, int subCol) {
        assertRowAndCol(subRow, subCol);

        return subCol * 2 + subRow * 4;
    }

    /* package */ static int subMask(int subRow, int subCol) {
        assertRowAndCol(subRow, subCol);

        return 0b11 << subShift(subRow, subCol);
    }
}
