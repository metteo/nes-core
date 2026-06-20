package net.novaware.nes.core.io.inject;

import dagger.Module;

@Module(includes = {
    IoRegModule.class,
    IoMemModule.class
})
public interface IoModule {
}
