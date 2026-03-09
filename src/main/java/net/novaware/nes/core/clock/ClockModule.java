package net.novaware.nes.core.clock;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Provider;
import net.novaware.nes.core.BoardScope;

@Module
public interface ClockModule {

    @Provides
    @BoardScope
    static LoopedClockGenerator provideLoopedClockGenerator() {
        return new LoopedClockGenerator();
    }

    @Provides
    @BoardScope
    static ClockGenerator provideClockGenerator(
        ClockMode clockMode,
        Provider<LoopedClockGenerator> loopedClockGenerator
    ) {
        return switch(clockMode) {
            case LOOP -> loopedClockGenerator.get();
            default -> throw new IllegalArgumentException("unknown clock mode: " + clockMode);
        };
    }
}
