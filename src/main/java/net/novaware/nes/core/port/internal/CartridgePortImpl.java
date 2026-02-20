package net.novaware.nes.core.port.internal;

import jakarta.inject.Named;
import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.port.CartridgePort;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static net.novaware.nes.core.cpu.memory.MemoryModule.CPU_BUS;

public class CartridgePortImpl implements CartridgePort {

    private MemoryBus cpuBus;
    // ppu bus too

    private @Nullable Cartridge cartridge;

    public CartridgePortImpl(
            @Named(CPU_BUS) MemoryBus cpuBus
    ) {
        this.cpuBus = cpuBus;
    }

    @Override
    public void disconnect() {
        // TODO: disconnect the cpu and ppu bus from the cartridge
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public void connect(Cartridge cartridge) {
        this.cartridge = requireNonNull(cartridge, "cartridge cannot be null");

        Cartridge.Config config = this.cartridge.getConfig();

        // TODO: check if cartridge config compatible with the board config

        //cpuBus.attach(this.cartridge.getProgram());
        //TODO: ppuBus.attach(this.cartridge.getVideo());
    }
}
