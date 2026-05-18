package net.novaware.nes.core.easy.inject;

import dagger.Module;
import net.novaware.nes.core.cpu.inject.CpuRegModule;
import net.novaware.nes.core.cpu.inject.CpuPinModule;

@Module(includes = {
    CpuRegModule.class,
    EasyMemModule.class,
    CpuPinModule.class
})
public interface EasyCpuModule {
}
