package net.novaware.nes.core.easy;

import dagger.Component;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuSignalModule;
import net.novaware.nes.core.cpu.inject.CpuRegModule;

@BoardScope
@Component(modules = {
    EasyModule.class,
    CpuRegModule.class,
    CpuSignalModule.class
})
public abstract class EasyComp {

    public static EasyComp newEasyComp() {
        return DaggerEasyComp.builder()
                .build();
    }

    public abstract EasyBoard newEasyBoard();
}
