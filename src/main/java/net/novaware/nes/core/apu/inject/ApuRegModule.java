package net.novaware.nes.core.apu.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.apu.register.ApuRegFile;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.register.ByteRegister;

import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;

@Module
public interface ApuRegModule {

    @Provides
    @BoardScope
    static ApuRegFile provideApuRegFile(
        @DmaVar(OAM) ByteRegister oamDma
    ) {
        return new ApuRegFile(oamDma); // TODO: use @Inject instead
    }
}
