package net.novaware.nes.core.ppu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.signal.internal.EdgeDetector;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.pin.internal.LatchingPin;
import net.novaware.nes.core.register.BooleanRegister;

import static net.novaware.nes.core.cpu.signal.Signal.LOW;
import static net.novaware.nes.core.ppu.inject.PpuVarName.S0H;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VBI;

/**
 * PPU Required Dependencies module. Should be provided by the board config.
 * This module is used as a stub when running standalone PPU.
 */
@Module
public interface PpuDepModule {

    // TODO: figure out a more concise way to provide output pins for standalone mode.
    @Provides
    @BoardScope
    @PpuVar(VBI)
    static BooleanRegister provideVBlankInterruptReg() {
        return new BooleanRegister(VBI.name());
    }

    @Provides
    @BoardScope
    @PpuVar(VBI)
    static Pin provideVBlankInterruptPin(@PpuVar(VBI) BooleanRegister register) {
        EdgeDetector detector = new EdgeDetector(VBI.name(), LOW);
        return new LatchingPin(VBI.name(), detector, register::set);
    }

    @Provides
    @BoardScope
    @PpuVar(S0H)
    static BooleanRegister provideSprite0HitReg() {
        return new BooleanRegister(S0H.name());
    }

    @Provides
    @BoardScope
    @PpuVar(S0H)
    static Pin provideSprite0HitPin(@PpuVar(S0H) BooleanRegister register) {
        LevelDetector detector = new LevelDetector(S0H.name(), LOW);
        return new LatchingPin(S0H.name(), detector, register::set);
    }
}
