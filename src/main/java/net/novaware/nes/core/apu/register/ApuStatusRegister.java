package net.novaware.nes.core.apu.register;

import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.Register;

public class ApuStatusRegister extends Register {

    public ApuStatusRegister() {
        super("SNDCHN");
    }

    public ByteRegister toByteRegister() {
        throw new UnsupportedOperationException("not implemented!");
    }
}
