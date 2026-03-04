package net.novaware.nes.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.nes.core.clock.ClockMode;
import net.novaware.nes.core.clock.ClockModule;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.cpu.inject.CpuModule;
import net.novaware.nes.core.port.internal.PortModule;

@BoardScope
@Component(modules = {
    CpuModule.class,
    ClockModule.class,
    PortModule.class
})
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory(CoreConfig config, ClockMode clockMode) {
        return DaggerBoardFactory.builder()
                .coreConfig(config)
                .clockMode(clockMode) // TODO: only clock config should come from the outside, not the whole thing
                .build();
    }

    public abstract Board newBoard();

    // TODO: consider adding a newCartridge method here. But then rename BoardFactory into sth else like NesCore

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder coreConfig(CoreConfig config);

        @BindsInstance
        public abstract Builder clockMode(ClockMode clockMode);

        public abstract BoardFactory build();
    }
}
