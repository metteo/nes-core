package net.novaware.nes.core.ppu.register;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.register.BooleanPipeline;
import net.novaware.nes.core.register.BooleanRegister;

import static net.novaware.nes.core.ppu.inject.PpuVarName.EB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EG;
import static net.novaware.nes.core.ppu.inject.PpuVarName.ER;
import static net.novaware.nes.core.ppu.inject.PpuVarName.GS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;

@BoardScope
public class PpuMaskRegister {

    public final BooleanRegister emphasizeRed;
    public final BooleanRegister emphasizeGreen;
    public final BooleanRegister emphasizeBlue;
    public final BooleanPipeline renderSprite;
    public final BooleanPipeline renderBackground;
    public final BooleanRegister maskSprite;
    public final BooleanRegister maskBackground;
    public final BooleanRegister greyscale;

    @Inject
    public PpuMaskRegister(
        @PpuVar(ER) BooleanRegister emphasizeRed,
        @PpuVar(EG) BooleanRegister emphasizeGreen,
        @PpuVar(EB) BooleanRegister emphasizeBlue,
        @PpuVar(RS) BooleanPipeline renderSprite,
        @PpuVar(RB) BooleanPipeline renderBackground,
        @PpuVar(MS) BooleanRegister maskSprite,
        @PpuVar(MB) BooleanRegister maskBackground,
        @PpuVar(GS) BooleanRegister greyscale
    ) {
        this.emphasizeRed = emphasizeRed;
        this.emphasizeGreen = emphasizeGreen;
        this.emphasizeBlue = emphasizeBlue;
        this.renderSprite = renderSprite;
        this.renderBackground = renderBackground;
        this.maskSprite = maskSprite;
        this.maskBackground = maskBackground;
        this.greyscale = greyscale;
    }
}
