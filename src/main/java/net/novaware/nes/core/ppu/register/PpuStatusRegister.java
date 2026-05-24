package net.novaware.nes.core.ppu.register;

import net.novaware.nes.core.register.BooleanPipeline;
import net.novaware.nes.core.register.Register;

import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;

public class PpuStatusRegister extends Register {

    private final BooleanPipeline verticalBlank = new BooleanPipeline(PS.doc() + ".V");
    private boolean spriteZeroHit;
    private boolean spriteOverflow;

    public PpuStatusRegister() {
        super(PS.doc());
    }

    public boolean isVerticalBlank() {
        return verticalBlank.get();
    }

    public void setVerticalBlank(boolean verticalBlank) {
        this.verticalBlank.set(verticalBlank);
    }

    public void setVerticalBlankDelayed(boolean verticalBlank, int delay) {
        this.verticalBlank.setDelayed(verticalBlank, delay);
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

    public void cycle() {
        verticalBlank.cycle();
    }

    @Override
    public String toString() {
        return getName() + ": " +
            (verticalBlank.get() ? "V" : "_") +
            (spriteZeroHit       ? "S" : "_") +
            (spriteOverflow      ? "O" : "_") +
            "_____";
    }
}
