package net.novaware.nes.core.register;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.sint;

public class ByteRegister extends DataRegister {

    private @Unsigned byte data;

    public ByteRegister(String name) {
        super(name);
    }

    public @Unsigned byte get() {
        return data;
    }

    public void set(@Unsigned byte data) {
        this.data = data;
    }

    public int getAsInt() {
        return sint(data);
    }

    public void setAsByte(int data) {
        set(ubyte(data));
    }
}
