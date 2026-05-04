package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.register.Register;

import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;

@BoardScope
public class PpuStatusRegister extends Register {

    private boolean verticalBlank;
    private boolean spriteZeroHit;
    private boolean spriteOverflow;

    public PpuStatusRegister() {
        super(PS.doc());
    }

    public boolean isVerticalBlank() {
        return verticalBlank;
    }

    public void setVerticalBlank(boolean verticalBlank) {
        this.verticalBlank = verticalBlank;
    }

    public boolean isSpriteZeroHit() {
        return spriteZeroHit;
    }

    public void setSpriteZeroHit(boolean spriteZeroHit) {
        this.spriteZeroHit = spriteZeroHit;
    }

    public boolean isSpriteOverflow() {
        return spriteOverflow;
    }

    public void setSpriteOverflow(boolean spriteOverflow) {
        this.spriteOverflow = spriteOverflow;
    }

    @Override
    public String toString() {
        return getName() + ": " +
            (verticalBlank   ? "V" : "_") +
            (spriteZeroHit   ? "S" : "_") +
            (spriteOverflow  ? "O" : "_") +
            "_____";
    }
}
