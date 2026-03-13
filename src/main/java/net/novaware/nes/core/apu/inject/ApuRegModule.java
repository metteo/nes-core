package net.novaware.nes.core.apu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.apu.register.ApuRegFile;

@Module
public interface ApuRegModule {

    @Provides
    @BoardScope
    static ApuRegFile provideApuRegFile() {
        return new ApuRegFile(); // TODO: use @Inject instead
    }
}
