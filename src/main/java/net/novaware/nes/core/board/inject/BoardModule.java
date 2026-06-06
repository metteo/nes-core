package net.novaware.nes.core.board.inject;

import dagger.Module;

@Module(includes = {
    BoardPinModule.class,
    BoardMemModule.class
})
public interface BoardModule {

}
