package net.novaware.nes.core.register;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class ByteRegister extends Register {

    private @Unsigned byte data;

    public ByteRegister(String name) {
        super(name);
    }

    public @Unsigned byte get() {
        return data;
    }

    public int getAsInt() {
        return uint(data);
    }

    public void set(@Unsigned byte data) {
        this.data = data;
    }

    public void setAsByte(int data) {
        set(ubyte(data));
    }
}
