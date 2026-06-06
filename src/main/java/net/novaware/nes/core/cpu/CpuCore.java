package net.novaware.nes.core.cpu;

import dagger.Component;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuDepModule;
import net.novaware.nes.core.cpu.inject.CpuModule;

@BoardScope
@Component(modules = {
    CpuModule.class,
    CpuDepModule.class
})
public abstract class CpuCore {

    public static CpuCore newCpuCore() {
        return DaggerCpuCore.create();
    }

    public abstract Cpu newCpu();
}
