package net.novaware.nes.core.ppu.inject;

import dagger.Module;

@Module(includes = {
    PpuRegModule.class,
    PpuMemModule.class
})
public interface PpuModule {
    // NOTE: do not add provide / bind methods here.
}
