package net.novaware.nes.core.apu.inject;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.apu.register.ApuRegFile;
import net.novaware.nes.core.register.IntegerCounter;

@Module
public interface ApuRegModule {

    @Provides
    @BoardScope
    @Named("APU.CC")
    static IntegerCounter provideCycleCounter() {
        return new IntegerCounter("APU.CC");
    }

    @Provides
    @BoardScope
    static ApuRegFile provideApuRegFile() {
        return new ApuRegFile(); // TODO: use @Inject instead
    }
}
