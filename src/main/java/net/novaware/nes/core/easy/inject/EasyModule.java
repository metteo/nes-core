package net.novaware.nes.core.easy.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;

import static net.novaware.nes.core.cpu.inject.CpuVarName.RES;
import static net.novaware.nes.core.cpu.signal.Signal.LOW;

@Module
public interface EasyModule {

    @Provides
    @BoardScope
    @CpuVar(RES)
    static LevelDetector provideResetDetector() {
        return new LevelDetector(RES.doc(), LOW);
    }
}
