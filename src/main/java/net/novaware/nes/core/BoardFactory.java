package net.novaware.nes.core;

import dagger.Component;

@BoardScope
@Component
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory() {
        return DaggerBoardFactory.builder()
                .build();
    }

    public abstract Board newBoard();
}
