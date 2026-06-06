package net.novaware.nes.core.ppu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.ppu.register.PpuStatusRegister;
import net.novaware.nes.core.ppu.register.VideoOutRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister.Variant;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.register.ShortRegister;

import static net.novaware.nes.core.ppu.inject.PpuVarName.CB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CH;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CI;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CP;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.CV;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.DR;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.EG;
import static net.novaware.nes.core.ppu.inject.PpuVarName.ER;
import static net.novaware.nes.core.ppu.inject.PpuVarName.GS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.HB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.MS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OAM;
import static net.novaware.nes.core.ppu.inject.PpuVarName.OF;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RB;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RL;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RST;
import static net.novaware.nes.core.ppu.inject.PpuVarName.SC;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VOUT;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;
import static net.novaware.nes.core.ppu.inject.PpuVarName.W;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org</a>
 */
@Module
public interface PpuRegModule {

    @Provides
    @BoardScope
    @PpuVar(CC)
    static IntegerCounter provideCycleCounter() {
        return new IntegerCounter(CC.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(SC)
    static IntegerCounter provideScanLineCounter() {
        return new IntegerCounter(SC.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(DC)
    static IntegerCounter provideDotCounter() {
        return new IntegerCounter(DC.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(PS)
    static PpuStatusRegister provideStatus() {
        return new PpuStatusRegister();
    }

    @Provides
    @BoardScope
    @PpuVar(HB)
    static BooleanRegister provideHorizontalBlank() {
        return new BooleanRegister(HB.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(VX)
    static ViewPortRegister provideCurrentViewPort() {
        return new ViewPortRegister(VX.doc(), Variant.VX);
    }

    @Provides
    @BoardScope
    @PpuVar(T)
    static ViewPortRegister provideTempViewPort() {
        return new ViewPortRegister(T.doc(), Variant.T);
    }

    @Provides
    @BoardScope
    @PpuVar(W)
    static BooleanRegister provideSecondWrite() {
        return new BooleanRegister(W.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(DR)
    static ByteRegister provideDataReadBuffer() {
        return new ByteRegister(DR.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(CV)
    static BooleanRegister provideVBlankInterruptEnabled() {
        return new BooleanRegister(CV.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(CP)
    static BooleanRegister provideMasterSlaveSelect() {
        return new BooleanRegister(CP.doc()); // 0: read backdrop from EXT pins; 1: output color on EXT pins
    }

    @Provides
    @BoardScope
    @PpuVar(CH)
    static BooleanRegister provideSpriteSize() {
        return new BooleanRegister(CH.doc()); // 0: 8x8 pixels; 1: 8x16 pixels
    }

    @Provides
    @BoardScope
    @PpuVar(CB)
    static ShortRegister provideBackgroundPatternTable() {
        return new ShortRegister(CB.doc()); // $0000 OR $1000
    }

    @Provides
    @BoardScope
    @PpuVar(CS)
    static ShortRegister provideSpritePatternTable() {
        return new ShortRegister(CS.doc()); // $0000 OR $1000; ignored in 8x16 mode
    }

    @Provides
    @BoardScope
    @PpuVar(CI)
    static ByteRegister provideVRAMAddressIncrement() {
        return new ByteRegister(CI.doc()); // add 1, going across OR add 32, going down
    }

    @Provides
    @BoardScope
    @PpuVar(ER)
    static BooleanRegister provideEmphasizeRed() {
        return new BooleanRegister(ER.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(EG)
    static BooleanRegister provideEmphasizeGreen() {
        return new BooleanRegister(EG.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(EB)
    static BooleanRegister provideEmphasizeBlue() {
        return new BooleanRegister(EB.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(RS)
    static BooleanRegister provideRenderSprite() {
        return new BooleanRegister(RS.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(RB)
    static BooleanRegister provideRenderBackground() {
        return new BooleanRegister(RB.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(MS)
    static BooleanRegister provideMaskSprite() {
        return new BooleanRegister(MS.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(MB)
    static BooleanRegister provideMaskBackground() {
        return new BooleanRegister(MB.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(GS)
    static BooleanRegister provideGreyscale() {
        return new BooleanRegister(GS.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(OAM)
    static ByteRegister provideObjAttrMemoryAddress() {
        return new ByteRegister(OAM.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(OF)
    static BooleanRegister provideOddFrame() {
        return new BooleanRegister(OF.doc());
    }

    /**
     * Prevents changes in PPU CTRL and MASK registers after reset until first pre-render scanline
     */
    @Provides
    @BoardScope
    @PpuVar(RL)
    static BooleanRegister provideResetLock() {
        return new BooleanRegister(RL.doc());
    }

    @Provides
    @BoardScope
    @PpuVar(RST)
    static BooleanRegister provideRstRegister() {
        return new BooleanRegister(RST.name());
    }

    @Provides
    @BoardScope
    static VideoOutRegister provideVideoOutRegister() {
        return new VideoOutRegister(VOUT.doc());
    }
}
