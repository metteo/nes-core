package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.register.Register;
import net.novaware.nes.core.util.Bin;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.UTypes.sint;
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

    // V / T: 0b0yyy_NNYY_YYYX_XXXX
    // X: xxx

    private int nameTable;  // 0-3, 0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00
    private int coarseY;    // 0-29/30-31
    private int coarseX;    // 0-31
    private int fineY;      // 0-7
    private int fineX;      // 0-7

    public ViewPortRegister(String name, Variant variant) {
        super(name);

        this.variant = variant;
    }

    public void high(@Unsigned byte hi) {
        int hiInt = sint(hi);

        int fY = (hiInt >> 4) & FINE_MASK;
        int nt = (hiInt >> 2) & NAMETABLE_MASK;
        int cY = (hiInt & 0b11) << 3;

        this.fineY = fY;
        this.nameTable = nt;

        this.coarseY = (coarseY & 0b111) | cY;
    }

    public void low(@Unsigned byte lo) {
        int loInt = sint(lo);

        int cX = loInt & COARSE_MASK;
        int cY = (loInt >> 5) & 0b111;

        this.coarseX = cX;
        this.coarseY = (coarseY & 0b11000) | cY;
    }

    public @Unsigned short get() {
        int cX = coarseX & COARSE_MASK;
        int cY = (coarseY & COARSE_MASK) << 5;
        int nt = (nameTable & NAMETABLE_MASK) << 10;
        int fY = (fineY & FINE_MASK) << 12;
        return ushort(fY | nt | cY | cX);
    }

    public void set(@Unsigned short address) {
        int addrInt = sint(address);

        coarseX = addrInt & COARSE_MASK;
        coarseY = (addrInt >> 5) & COARSE_MASK;
        nameTable = (addrInt >> 10) & NAMETABLE_MASK;
        fineY = (addrInt >> 12) & FINE_MASK;
    }

    public int getNameTable() {
        return nameTable & NAMETABLE_MASK;
    }

    public void setNameTable(int nametable) {
        this.nameTable = nametable & NAMETABLE_MASK;
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

    public void transfer(ViewPortRegister target) {
        assertState(
            this.variant == Variant.T && target.variant == Variant.VX,
            "only T -> V transfer is allowed"
        );

        target.coarseY   = this.coarseY;
        target.coarseX   = this.coarseX;
        target.nameTable = this.nameTable;
        target.fineY     = this.fineY;
        // fineX is not transferred
    }

    @Override
    public String toString() {
        return getName() + ": 0b" + Bin.s(get());
    }
}
