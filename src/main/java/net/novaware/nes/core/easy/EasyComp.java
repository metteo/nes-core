package net.novaware.nes.core.easy;

import dagger.Component;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.easy.inject.EasyCpuModule;
import net.novaware.nes.core.easy.inject.EasyModule;

@BoardScope
@Component(modules = {
    EasyModule.class,
    EasyCpuModule.class,
})
public abstract class EasyComp {

    public static EasyComp newEasyComp() {
        return DaggerEasyComp.builder()
                .build();
    }

    public abstract EasyBoard newEasyBoard();
}
