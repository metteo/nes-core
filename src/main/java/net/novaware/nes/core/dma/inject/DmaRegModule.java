package net.novaware.nes.core.dma.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.register.ByteRegister;

import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;

@Module
public interface DmaRegModule {

    @Provides
    @BoardScope
    @DmaVar(OAM)
    static ByteRegister provideOamDmaRegister() {
        return new ByteRegister("OAMDMA"); // 0x4014
    }
}
