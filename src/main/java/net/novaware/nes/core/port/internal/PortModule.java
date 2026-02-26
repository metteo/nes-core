package net.novaware.nes.core.port.internal;

import dagger.Binds;
import dagger.Module;
import net.novaware.nes.core.port.CartridgePort;
import net.novaware.nes.core.port.DebugPort;

@Module
public interface PortModule {

    @Binds
    CartridgePort bindCartridgePort(CartridgePortImpl cartridge);

    @Binds
    DebugPort bindDebugPort(DebugPortImpl debug);
}
