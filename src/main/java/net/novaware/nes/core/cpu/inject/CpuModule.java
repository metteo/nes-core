package net.novaware.nes.core.cpu.inject;

import dagger.Module;

/**
 * Aggregating module for the CPU
 */
@Module(includes = {
    CpuRegModule.class,
    CpuMemModule.class,
    CpuPinModule.class
})
public interface CpuModule {
    // NOTE: do not add provide / bind methods here.
}
