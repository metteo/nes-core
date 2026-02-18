package net.novaware.nes.core.cart;

import net.novaware.nes.core.cart.internal.CartridgeImpl;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.memory.MemoryDevice;

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

    MemoryDevice getProgram();

    MemoryDevice getVideo();

    static Cartridge of(NesFile nesFile) {
        return new CartridgeImpl(nesFile);
    }

    // TODO: expose IRQ from the processor
    // TODO: maybe add clock?
    // TODO: add store method to save SRAM/NVVRAM (call on emu/game close, game change, power off / reset)
    // TODO: sram store heuristic: 5s delay starting from a write, don't reset the delay on following writes
    // TODO: try to load a save state.
    // TODO: support save slots, make it convenient to choose / change slot for games that only have 1 internal
}
