package net.novaware.nes.core.ppu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.Register;

@BoardScope
public class PpuStatusRegister extends Register {

    @Inject
    public PpuStatusRegister() {
        super("PPU_STATUS");
    }

    public ByteRegister toByteRegister() {
        throw new UnsupportedOperationException("not implemented!");
    }
}
