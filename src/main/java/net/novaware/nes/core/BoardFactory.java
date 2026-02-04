package net.novaware.nes.core;

import dagger.Component;
import net.novaware.nes.core.cpu.CpuModule;

@BoardScope
@Component(modules = { CpuModule.class })
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory() {
        return DaggerBoardFactory.builder()
                .build();
    }

    public abstract Board newBoard();
}
