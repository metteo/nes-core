package net.novaware.nes.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.nes.core.port.internal.PortModule;
import net.novaware.nes.core.clock.ClockMode;
import net.novaware.nes.core.clock.ClockModule;
import net.novaware.nes.core.cpu.CpuModule;
import net.novaware.nes.core.cpu.memory.MemoryModule;

@BoardScope
@Component(modules = {
    CpuModule.class,
    MemoryModule.class,
    ClockModule.class,
    PortModule.class
})
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory(ClockMode clockMode) {
        return DaggerBoardFactory.builder()
                .clockMode(clockMode) // TODO: only clock config should come from the outside, not the whole thing
                .build();
    }

    public abstract Board newBoard();

    // TODO: consider adding a newCartridge method here. But then rename BoardFactory into sth else like NesCore

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder clockMode(ClockMode clockMode);

        public abstract BoardFactory build();
    }
}
