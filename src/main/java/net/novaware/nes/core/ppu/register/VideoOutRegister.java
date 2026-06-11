package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.register.Register;
import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Represents VOUT physical pin in PPU
 */
public class VideoOutRegister extends Register {

    private int y;
    private int x;
    private @Unsigned byte colorIndex;

    public VideoOutRegister(String name) {
        super(name);
    }

    public void set(int y, int x, @Unsigned byte colorIndex) {
        this.y = y;
        this.x = x;
        this.colorIndex = colorIndex;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public @Unsigned byte getColorIndex() {
        return colorIndex;
    }
}
