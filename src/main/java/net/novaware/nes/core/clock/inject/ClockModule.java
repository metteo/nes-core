package net.novaware.nes.core.clock.inject;

import dagger.Binds;
import dagger.Module;
import jakarta.inject.Named;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.apu.Apu;
import net.novaware.nes.core.clock.ClockGenerator;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.clock.MasterClock;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.dma.Dma;
import net.novaware.nes.core.ppu.Ppu;

@Module
public interface ClockModule {

    @Binds
    @BoardScope
    ClockGenerator bindMasterClock(MasterClock masterClock);

    @Binds
    @BoardScope
    @Named("CPU")
    ClockReceiver bindCpu(Cpu cpu);

    @Binds
    @BoardScope
    @Named("PPU")
    ClockReceiver bindPpu(Ppu ppu);

    @Binds
    @BoardScope
    @Named("APU")
    ClockReceiver bindApu(Apu apu);

    @Binds
    @BoardScope
    @Named("DMA")
    ClockReceiver bindDma(Dma dma);
}
