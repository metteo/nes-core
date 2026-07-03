package net.novaware.nes.core.ppu.unit;

import net.novaware.nes.core.ppu.memory.ExtBus;
import org.jspecify.annotations.Nullable;

public class RenderUnit {

    // background shift regs
    private short tileHighPlane;
    private short tileLowPlane;
    private byte attribute;

    private @Nullable SpriteOutput spriteOutput0; // -8 / 16
}
