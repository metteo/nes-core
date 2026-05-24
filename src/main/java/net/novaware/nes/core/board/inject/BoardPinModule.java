package net.novaware.nes.core.board.inject;

import dagger.Binds;
import dagger.Module;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.inject.CpuVarName;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.inject.PpuVarName;

import static net.novaware.nes.core.cpu.inject.CpuVarName.NMI;

@Module
public interface BoardPinModule {

    @Binds
    @BoardScope
    @PpuVar(PpuVarName.VBI)
    Pin bindVBlankIntPin(@CpuVar(NMI) Pin cpuNmi);

    @Binds
    @BoardScope
    @PpuVar(PpuVarName.S0H)
    Pin bindSprite0HitPin(@CpuVar(CpuVarName.S0H) Pin sprite0Hit);
}
