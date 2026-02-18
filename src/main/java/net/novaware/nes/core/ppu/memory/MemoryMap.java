package net.novaware.nes.core.ppu.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.sint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_memory_map">PPU Memory Map on nesdev.org</a>
 */
public class MemoryMap {

    public static final @Unsigned short MEMORY_START = ushort(0x0000);
    public static final @Unsigned short MEMORY_END = ushort(0x3FFF);
    public static final int MEMORY_SIZE = sint(MEMORY_END) - sint(MEMORY_START) + 1;

    public static final @Unsigned short PATTERN_TABLE_1_START = ushort(0x0000);
    public static final @Unsigned short PATTERN_TABLE_1_END = ushort(0x0FFF);
    public static final int PATTERN_TABLE_1_SIZE = sint(PATTERN_TABLE_1_END) - sint(PATTERN_TABLE_1_START) + 1;

    public static final @Unsigned short PATTERN_TABLE_2_START = ushort(0x1000);
    public static final @Unsigned short PATTERN_TABLE_2_END = ushort(0x1FFF);
    public static final int PATTERN_TABLE_2_SIZE = sint(PATTERN_TABLE_2_END) - sint(PATTERN_TABLE_2_START) + 1;

    public static final @Unsigned short VRAM_START = ushort(0x2000);
    public static final @Unsigned short VRAM_END = ushort(0x2FFF);
    public static final int VRAM_SIZE = sint(VRAM_END) - sint(VRAM_START) + 1;

    // TODO: maybe add subsections of vram (name / attribute tables 0-3)

    public static final @Unsigned short UNUSED_START = ushort(0x3000);
    public static final @Unsigned short UNUSED_END = ushort(0x3EFF);
    public static final int UNUSED_SIZE = sint(UNUSED_END) - sint(UNUSED_START) + 1;

    public static final @Unsigned short PALETTE_RAM_START = ushort(0x3F00);
    public static final @Unsigned short PALETTE_RAM_END = ushort(0x3F1F);
    public static final int PALETTE_RAM_SIZE = sint(PALETTE_RAM_END) - sint(PALETTE_RAM_START) + 1;

    public static final @Unsigned short PALETTE_RAM_MIRROR_START = ushort(0x3F20);
    public static final @Unsigned short PALETTE_RAM_MIRROR_END = ushort(0x3FFF);
    public static final int PALETTE_RAM_MIRROR_SIZE = sint(PALETTE_RAM_MIRROR_END) - sint(PALETTE_RAM_MIRROR_START) + 1;
}
