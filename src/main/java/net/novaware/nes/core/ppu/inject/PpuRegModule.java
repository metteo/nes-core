package net.novaware.nes.core.ppu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.ppu.register.PpuRegFile;

@Module
public interface PpuRegModule {

    @Provides
    @BoardScope
    static PpuRegFile providePpuRegFile() { // TODO: use @Inject instead
        return new PpuRegFile();
    }
}
