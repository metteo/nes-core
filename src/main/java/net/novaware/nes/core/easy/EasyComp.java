package net.novaware.nes.core.easy;

import dagger.Component;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuModule;

@BoardScope
@Component(modules = { CpuModule.class, EasyModule.class })
public abstract class EasyComp {

    public static EasyComp newEasyComp() {
        return DaggerEasyComp.builder()
                .build();
    }

    public abstract EasyBoard newEasyBoard();
}
