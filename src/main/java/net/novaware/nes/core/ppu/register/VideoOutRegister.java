package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.register.Register;
import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Represents VOUT physical pin in PPU
 */
public class VideoOutRegister extends Register {

    private int y;
    private int x;
    private @Unsigned byte colorRef;

    public VideoOutRegister(String name) {
        super(name);
    }

    public void set(int y, int x, @Unsigned byte colorRef) {
        this.y = y;
        this.x = x;
        this.colorRef = colorRef;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public @Unsigned byte getColorRef() {
        return colorRef;
    }
}
