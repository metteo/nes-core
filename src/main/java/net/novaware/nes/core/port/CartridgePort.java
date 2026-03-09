package net.novaware.nes.core.port;

import net.novaware.nes.core.cart.Cartridge;

public interface CartridgePort extends InputPort, OutputPort {

    void disconnect();

    void connect(Cartridge cartridge);
}
