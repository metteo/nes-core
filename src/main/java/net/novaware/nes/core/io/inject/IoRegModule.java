package net.novaware.nes.core.io.inject;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;

@Module
public interface IoRegModule {

    @Provides
    @BoardScope
    @Named("JOY1_DATA")
    static ByteRegister provideJoy1Data() {
        return new ByteRegister("JOY1_DATA");
    }

    @Provides
    @BoardScope
    @Named("JOY2_DATA")
    static ByteRegister provideJoy2Data() {
        return new ByteRegister("JOY2_DATA");
    }

    @Provides
    @BoardScope
    @Named("JOY_STROBE")
    static BooleanRegister provideJoyStrobe() {
        return new BooleanRegister("JOY_STROBE");
    }
}
