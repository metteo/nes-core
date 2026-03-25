package net.novaware.nes.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.nes.core.apu.inject.ApuModule;
import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.cart.internal.CartridgeImpl;
import net.novaware.nes.core.clock.ClockMode;
import net.novaware.nes.core.clock.ClockModule;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.cpu.inject.CpuModule;
import net.novaware.nes.core.dma.inject.DmaModule;
import net.novaware.nes.core.file.ReaderMode;
import net.novaware.nes.core.file.ines.NesFileReader;
import net.novaware.nes.core.file.ines.NesFileReadingException;
import net.novaware.nes.core.port.internal.PortModule;
import net.novaware.nes.core.ppu.inject.PpuModule;

import java.net.URI;

@BoardScope
@Component(modules = {
    DmaModule.class,
    ApuModule.class,
    PpuModule.class,
    CpuModule.class,
    ClockModule.class,
    PortModule.class
})
public abstract class BoardFactory { // TODO: rename BoardFactory into sth else like NesCore

    public static BoardFactory newBoardFactory(CoreConfig config, ClockMode clockMode) {
        return DaggerBoardFactory.builder()
                .coreConfig(config)
                .clockMode(clockMode) // TODO: only clock config should come from the outside, not the whole thing
                .build();
    }

    public abstract Board newBoard();

    public Cartridge newCartridge(URI file) { // TODO: improve, for now only for nestest
        NesFileReader.Result result = new NesFileReader().read(file, ReaderMode.LENIENT);
        if (!result.problems().isEmpty() || result.nesFile() == null) {
            throw new NesFileReadingException(result.problems());
        }
        return new CartridgeImpl(result.nesFile());
    }

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder coreConfig(CoreConfig config);

        @BindsInstance
        public abstract Builder clockMode(ClockMode clockMode);

        public abstract BoardFactory build();
    }
}
