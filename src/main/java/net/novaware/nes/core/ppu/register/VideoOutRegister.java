package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.register.Register;
import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Represents VOUT physical pin in PPU
 */
public class VideoOutRegister extends Register {

    public VideoOutRegister(String name) {
        super(name);
    }

    public void set(int y, int x, @Unsigned byte colorIndex) {

    }
}
