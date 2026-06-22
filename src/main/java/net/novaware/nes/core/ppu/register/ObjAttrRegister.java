package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.register.Register;
import net.novaware.nes.core.util.Hex;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Nums.powOfTwo;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 *  Primary & Secondary OAM Address register type
 */
public class ObjAttrRegister extends Register {

    private @Unsigned byte data;
    private @Unsigned byte mask;

    /**
     * @param size in pow of 2 bytes
     */
    public ObjAttrRegister(String name, int size) {
        super(name);

        assertArgument(0 < size && size <= 0x100, "size out of range");
        assertArgument(powOfTwo(size), "size is not power of 2");

        mask = ubyte(size - 1);
    }

    public @Unsigned byte get() {
        return data;
    }

    public void set(@Unsigned byte data) {
        this.data = ubyte(sint(data) & sint(mask));
    }

    public int getAsInt() {
        return sint(data);
    }

    public void setAsByte(int data) {
        set(ubyte(data));
    }

    @Override
    public String toString() {
        return getName() + ": 0x" + Hex.s(get());
    }
}
