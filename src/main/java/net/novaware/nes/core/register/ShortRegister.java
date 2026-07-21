package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.UBYTE_MASK;
import static net.novaware.nes.core.util.UTypes.USHORT_MASK;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

public final class ShortRegister extends Register {

    /*
     * Internally we keep 2 bytes because that's how it works in hardware
     * Using ints to limit casting when assembling full short
     */
    private int hi;
    private int lo;

    public ShortRegister(String name) {
        super(name);
    }

    public @Unsigned short get() {
        return ushort(hi << 8 | lo); // duplicated to limit method calls
    }

    public @Unsigned byte high() {
        return ubyte(hi);
    }

    public @Unsigned byte low() {
        return ubyte(lo);
    }

    public int getAsInt() {
        return (hi << 8 | lo) & USHORT_MASK;
    }

    public int highAsInt() {
        return hi;
    }

    public int lowAsInt() {
        return lo;
    }

    public void set(@Unsigned short address) {
        setAsShort(sint(address));
    }

    public ShortRegister high(@Unsigned byte hi) {
        this.hi = sint(hi);

        return this;
    }

    public ShortRegister low(@Unsigned byte lo) {
        this.lo = sint(lo);

        return this;
    }

    public void setAsShort(int address) {
        hi = (address & 0xFF00) >> 8;
        lo =  address & 0x00FF;
    }

    public ShortRegister highAsByte(int hi) {
        this.hi = hi & UBYTE_MASK;

        return this;
    }

    public ShortRegister lowAsByte(int lo) {
        this.lo = lo & UBYTE_MASK;

        return this;
    }

    @Override
    public String toString() {
        return getName() + ": 0x" + Hex.s(get());
    }
}
