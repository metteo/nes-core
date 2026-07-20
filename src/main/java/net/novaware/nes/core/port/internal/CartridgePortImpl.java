package net.novaware.nes.core.port.internal;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cart.Cartridge;
import net.novaware.nes.core.cpu.memory.CpuBus;
import net.novaware.nes.core.memory.BankedMemory;
import net.novaware.nes.core.port.CartridgePort;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.memory.PpuBus;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VRAM;

@BoardScope
public class CartridgePortImpl implements CartridgePort {

    private CpuBus cpuBus;

    private BankedMemory ppuVideoMemory;
    private PpuBus ppuBus;

    private @Nullable Cartridge cartridge; // TODO: replace with null object

    @Inject
    public CartridgePortImpl(
        CpuBus cpuBus,
        @PpuVar(VRAM) BankedMemory ppuVideoMemory,
        PpuBus ppuBus
    ) {
        this.cpuBus = cpuBus;
        this.ppuVideoMemory = ppuVideoMemory;
        this.ppuBus = ppuBus;
    }

    @Override
    public void disconnect() {
        // TODO: disconnect the cpu and ppu bus from the cartridge
        if (cartridge != null) { cartridge.disconnect(); }
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public void connect(Cartridge cartridge) {
        this.cartridge = requireNonNull(cartridge, "cartridge cannot be null");

        Cartridge.Config config = cartridge.getConfig();

        // TODO: check if cartridge config compatible with the board config


        cpuBus.attachCartridge(cartridge.getCpuBusDevice());
        ppuBus.attachCartridge(cartridge.getPpuBusDevice(ppuVideoMemory));
    }


}
