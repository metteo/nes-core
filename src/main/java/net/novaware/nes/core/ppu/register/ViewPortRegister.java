package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.register.Register;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class ViewPortRegister extends Register {

    public enum Variant { // TODO: consider CURRENT / TEMPORARY values instead
        /**
         *
         */
        VX,
        /**
         *
         */
        T
    }

    public static final int NAMETABLE_MASK = 0b11;
    public static final int COARSE_MASK = 0b1_1111;
    public static final int FINE_MASK = 0b111;

    private final Variant variant;





    // FIXME: temporary, use the ints below
    private @Unsigned byte hi;
    private @Unsigned byte lo;






    // V / T: yyy NN YYYYY XXXXX
    // X: xxx

    private int nametable;  // 0-3, 0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00
    private int coarseY;    // 0-29/30-31
    private int coarseX;    // 0-31
    private int fineY;      // 0-7
    private int fineX;      // 0-7

    public ViewPortRegister(String name, Variant variant) {
        super(name);

        this.variant = variant;
    }

    public void high(@Unsigned byte hi) {
        this.hi = hi;
    }

    public void low(@Unsigned byte lo) {
        this.lo = lo;
    }

    public @Unsigned short get() {
        return ushort(sint(hi) << 8 | sint(lo));
    }

    public void set(@Unsigned short address) {
        hi = ubyte((address & 0xFF00) >> 8);
        lo = ubyte(address & 0x00FF);
    }

    public int getNametable() {
        return nametable & NAMETABLE_MASK;
    }

    public void setNametable(int nametable) {
        this.nametable = nametable & NAMETABLE_MASK;
    }

    public int getCoarseY() {
        return coarseY & COARSE_MASK;
    }

    public void setCoarseY(int coarseY) {
        this.coarseY = coarseY & COARSE_MASK;
    }

    public int getCoarseX() {
        return coarseX & COARSE_MASK;
    }

    public void setCoarseX(int coarseX) {
        this.coarseX = coarseX & COARSE_MASK;
    }

    public int getFineY() {
        return fineY & FINE_MASK;
    }

    public void setFineY(int fineY) {
        this.fineY = fineY & FINE_MASK;
    }

    public int getFineX() {
        assertState(variant != Variant.T, "T variant doesn't have fineX component");

        return fineX & FINE_MASK;
    }

    public void setFineX(int fineX) {
        assertState(variant != Variant.T, "T variant doesn't have fineX component");

        this.fineX = fineX & FINE_MASK;
    }

    @Override
    public String toString() {
        return getName() + ": 0x" + Hex.s(get());
    }
}
