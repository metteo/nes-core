package net.novaware.nes.core.cpu.inject;

import dagger.Module;

/**
 * Aggregating module for the CPU
 */
@Module(includes = {
    RegisterModule.class,
    MemoryModule.class
})
public interface CpuModule {
    // NOTE: do not add provide methods here.
}
