package net.novaware.nes.core;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.util.uml.Owned;

@BoardScope
public class Board {
    // TODO: setup register files for cpu, ppu, apu and any other
    // TODO: any container of data should be wrapped in an object and injectable (e.g. into a debug interface)

    @Owned
    private final Cpu cpu;

    @Inject
    /* package */ Board(
        final Cpu cpu
    ) {
        this.cpu = cpu;
    }
}
