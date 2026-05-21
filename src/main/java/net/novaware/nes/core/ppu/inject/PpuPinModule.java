package net.novaware.nes.core.ppu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.pin.internal.ReactivePin;
import net.novaware.nes.core.register.BooleanRegister;

import static net.novaware.nes.core.ppu.inject.PpuVarName.RST;

@Module
public interface PpuPinModule {

    @Provides
    @BoardScope
    @PpuVar(RST)
    static Pin provideRstPin(@PpuVar(RST) BooleanRegister register) {
        LevelDetector detector = new LevelDetector(RST.doc(), Signal.LOW);
        return new ReactivePin(RST.doc(), detector, register::set);
    }
}
