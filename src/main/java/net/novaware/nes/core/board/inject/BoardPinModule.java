package net.novaware.nes.core.board.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.inject.CpuVarName;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.inject.PpuVarName;

import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.ppu.inject.PpuVarName.RST;

@Module
public interface BoardPinModule {

    @Binds
    @PpuVar(PpuVarName.VBI)
    Pin bindVBlankIntPin(@CpuVar(NMI) EdgeDetector nmiDetector);

    @Binds
    @BoardScope
    @PpuVar(PpuVarName.S0H)
    Pin bindSprite0HitPin(@CpuVar(CpuVarName.S0H) LevelDetector sprite0Hit);

    @Provides
    @BoardScope
    @Named("BRD.RST")
    static LevelDetector provideResetDetector() {
        return new LevelDetector("BRD.RST", LOW);
    }

    @Binds
    @BoardScope
    @CpuVar(CpuVarName.RES)
    LevelDetector bindCpuResetDetector(@Named("BRD.RST") LevelDetector reset);

    @Binds
    @BoardScope
    @PpuVar(RST)
    LevelDetector bindPpuResetDetector(@Named("BRD.RST") LevelDetector reset);
}
