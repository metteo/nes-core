package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

public class ShortRegister extends AddressRegister {

    /*
     * Internally we keep 2 bytes because that's how it works in hardware
     */
    private @Unsigned byte hi;
    private @Unsigned byte lo;

    public ShortRegister(String name) {
        super(name);
    }

    public @Unsigned short get() {
        return ushort(getAsInt());
    }

    @Override
    public @Unsigned byte high() {
        return hi;
    }

    @Override
    public @Unsigned byte low() {
        return lo;
    }

    public int getAsInt() {
        return sint(hi) << 8 | sint(lo);
    }

    @Override
    public int highAsInt() {
        return sint(hi);
    }

    @Override
    public int lowAsInt() {
        return sint(lo);
    }

    public void set(@Unsigned short address) {
        setAsShort(sint(address));
    }

    @Override
    public AddressRegister high(@Unsigned byte hi) {
        this.hi = hi;

        return this;
    }

    @Override
    public AddressRegister low(@Unsigned byte lo) {
        this.lo = lo;

        return this;
    }

    public void setAsShort(int address) {
        hi = ubyte((address & 0xFF00) >> 8);
        lo = ubyte(address & 0x00FF);
    }

    @Override
    public AddressRegister highAsByte(int hi) {
        this.hi = ubyte(hi);

        return this;
    }

    @Override
    public AddressRegister lowAsByte(int lo) {
        this.lo = ubyte(lo);

        return this;
    }

    @Override
    public String toString() {
        return getName() + ": 0x" + Hex.s(get());
    }
}
