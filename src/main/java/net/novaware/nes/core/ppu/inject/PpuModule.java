package net.novaware.nes.core.ppu.inject;

import dagger.Module;

@Module(includes = {
    PpuRegModule.class,
    PpuPinModule.class,
    PpuMemModule.class,
    PpuTabModule.class
})
public interface PpuModule {
    // NOTE: do not add provide / bind methods here.
}
