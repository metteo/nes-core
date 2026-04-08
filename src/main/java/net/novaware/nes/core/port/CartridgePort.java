package net.novaware.nes.core.port;

import net.novaware.nes.core.cart.Cartridge;

/**
 * @see <a href="https://www.nesdev.org/wiki/Cartridge_connector">Cartridge connector on nesdev.org</a>
 */
public interface CartridgePort extends InputPort, OutputPort {

    void disconnect();

    void connect(Cartridge cartridge);
}
