package net.novaware.nes.core.cart;

import net.novaware.nes.core.cart.internal.CartridgeImpl;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.signal.internal.Detector;
import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.memory.BankedMemory;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.port.CartridgePort;

/**
 * @see <a href="https://www.nesdev.org/wiki/Cartridge_connector">Cartridge connector on nesdev.org</a>
 */
public interface Cartridge {

    interface Config {
        Platform getPlatform();

        VideoStandard getVideoStandard();

        Region getRegion();
    }

    Config getConfig();

    /**
     * CPU A0-A15, D0-D7, R/W
     */
    MemoryDevice.ReadWrite getCpuBusDevice(); // TODO: provide code and data segment registers somehow?

    /**
     * ___
     * IRQ (to CPU)
     */
    void setIrqDetector(Detector irqDetector); // TODO: consider moving from internal and change the name?

    /**
     * @param ppuVideoMemory CIRAM A10, /CE
     * @return PPU A0-A13, D0-D7, /RD, /WR
     */
    MemoryDevice.ReadWrite getPpuBusDevice(BankedMemory ppuVideoMemory);

    /**
     * @param in Audio from 2A03
     * @return out Audio to "RF"
     */
    default Object setAudio(Object in) { return in; } // TODO: placeholder, refine the typing when implementing APU

    /**
     * Called by {@link CartridgePort#disconnect()}
     */
    void disconnect();

    static Cartridge of(NesFile nesFile) {
        return new CartridgeImpl(nesFile);
    }

    // TODO: maybe add clock?
    // TODO: add store method to save SRAM/NVVRAM (call on emu/game close, game change, power off / reset)
    // TODO: sram store heuristic: 5s delay starting from a write, don't reset the delay on following writes
    // TODO: try to load a save state.
    // TODO: support save slots, make it convenient to choose / change slot for games that only have 1 internal
}
