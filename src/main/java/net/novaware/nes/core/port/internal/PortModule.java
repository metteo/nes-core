package net.novaware.nes.core.port.internal;

import dagger.Binds;
import dagger.Module;
import net.novaware.nes.core.port.CartridgePort;
import net.novaware.nes.core.port.DebugPort;
import net.novaware.nes.core.port.DisplayPort;

@Module
public interface PortModule { // TODO: consider adding @BoardScope to Binds methods

    @Binds
    CartridgePort bindCartridgePort(CartridgePortImpl cartridge);

    @Binds
    DebugPort bindDebugPort(DebugPortImpl debug);

    @Binds
    DisplayPort bindDisplayPort(DisplayPortImpl displayPort);
}
