package net.novaware.nes.core.easy.inject;

import dagger.Module;
import net.novaware.nes.core.cpu.inject.CpuRegModule;
import net.novaware.nes.core.cpu.inject.CpuSignalModule;

@Module(includes = {
    CpuRegModule.class,
    EasyMemModule.class,
    CpuSignalModule.class
})
public interface EasyCpuModule {
}
