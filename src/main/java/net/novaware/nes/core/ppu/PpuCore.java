package net.novaware.nes.core.ppu;

import dagger.Component;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.ppu.inject.PpuDepModule;
import net.novaware.nes.core.ppu.inject.PpuModule;

@BoardScope
@Component(modules = {
    PpuModule.class,
    PpuDepModule.class
})
public abstract class PpuCore {

    public static PpuCore newPpuCore() {
        return DaggerPpuCore.create();
    }

    public abstract Ppu newPpu();
}
