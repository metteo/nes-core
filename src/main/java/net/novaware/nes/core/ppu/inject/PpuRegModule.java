package net.novaware.nes.core.ppu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.ppu.register.PpuRegFile;
import net.novaware.nes.core.ppu.register.PpuStatusRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister.Variant;
import net.novaware.nes.core.register.BooleanRegister;

import static net.novaware.nes.core.ppu.inject.PpuVarName.CI;
import static net.novaware.nes.core.ppu.inject.PpuVarName.GS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.PS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;
import static net.novaware.nes.core.ppu.inject.PpuVarName.W;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_registers">PPU Registers on nesdev.org</a>
 */
@Module
public interface PpuRegModule {

    @Provides
    @BoardScope
    static PpuRegFile providePpuRegFile() { // TODO: use @Inject instead
        return new PpuRegFile();
    }

    @Provides
    @BoardScope
    @PpuVar(PS)
    static PpuStatusRegister provideStatus() {
        return new PpuStatusRegister();
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
    @PpuVar(CI) // TODO: switch to ByteRegister
    static BooleanRegister provideVideoMemoryAddressIncrement() {
        return new BooleanRegister("PPU.CTRL.I"); // true means increment by 32
    }

    @Provides
    @BoardScope
    @PpuVar(GS)
    static BooleanRegister provideGreyscale() {
        return new BooleanRegister("PPU.GS");
    }
}
