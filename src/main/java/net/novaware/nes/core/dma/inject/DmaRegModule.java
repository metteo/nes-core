package net.novaware.nes.core.dma.inject;

import dagger.Module;
import dagger.Provides;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.IntegerCounter;

import static net.novaware.nes.core.dma.inject.DmaVarName.CC;
import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;

@Module
public interface DmaRegModule {

    @Provides
    @BoardScope
    @DmaVar(CC)
    static IntegerCounter provideCycleCounter() {
        return new IntegerCounter(CC.doc());
    }

    @Provides
    @BoardScope
    @DmaVar(OAM)
    static ByteRegister provideOamDmaRegister() {
        return new ByteRegister("OAMDMA"); // 0x4014
    }
}
