package net.novaware.nes.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.nes.core.cpu.CpuModule;
import net.novaware.nes.core.cpu.unit.ClockGenerator;

@BoardScope
@Component(modules = { CpuModule.class })
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory(ClockGenerator clock) {
        return DaggerBoardFactory.builder()
                .clock(clock) // TODO: only clock config should come from the outside, not the whole thing
                .build();
    }

    public abstract Board newBoard();

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder clock(ClockGenerator clock);

        public abstract BoardFactory build();
    }
}
