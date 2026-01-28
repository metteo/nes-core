package net.novaware.nes.core.register;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

public class ShortRegister extends Register {

    private @Unsigned short data;

    public ShortRegister(String name) {
        super(name);
    }

    public @Unsigned short get() {
        return data;
    }

    public int getAsInt() {
        return uint(data);
    }

    public void set(@Unsigned short data) {
        this.data = data;
    }

    public void setAsShort(int data) {
        set(ushort(data));
    }
}
