package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.memory.MemoryMap;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/PPU_memory_map">PPU Memory Map on nesdev.org</a>
 */
public class PpuMemMap implements MemoryMap {

    // region Cartridge (CPU / DMA and PPU)

    public static final @Unsigned short MEMORY_START = ushort(0x0000);
    public static final @Unsigned short MEMORY_END = ushort(0x3FFF); // 14 bits
    public static final int MEMORY_SIZE = sint(MEMORY_END) - sint(MEMORY_START) + 1;

    // region CHR-[ROM/RAM]

    // TODO: PATTERN_TABLES start,end,size

    public static final @Unsigned short PATTERN_TABLE_0_START = ushort(0x0000);
    public static final @Unsigned short PATTERN_TABLE_0_END = ushort(0x0FFF);
    public static final int PATTERN_TABLE_0_SIZE = sint(PATTERN_TABLE_0_END) - sint(PATTERN_TABLE_0_START) + 1;

    public static final @Unsigned short PATTERN_TABLE_1_START = ushort(0x1000);
    public static final @Unsigned short PATTERN_TABLE_1_END = ushort(0x1FFF);
    public static final int PATTERN_TABLE_1_SIZE = sint(PATTERN_TABLE_1_END) - sint(PATTERN_TABLE_1_START) + 1;

    // endregion
    // region VRAM

    public static final @Unsigned short VRAM_START = ushort(0x2000);
    public static final @Unsigned short VRAM_END = ushort(0x2FFF);
    public static final int VRAM_SIZE = sint(VRAM_END) - sint(VRAM_START) + 1;

    public static final @Unsigned short LAYOUT_TABLE_0_START = VRAM_START;
    public static final @Unsigned short LAYOUT_TABLE_0_END = ushort(0x23BF);
    public static final int LAYOUT_TABLE_0_SIZE = sint(LAYOUT_TABLE_0_END) - sint(LAYOUT_TABLE_0_START) + 1;

    public static final @Unsigned short ATTRIBUTE_TABLE_0_START = ushort(0x23C0);
    public static final @Unsigned short ATTRIBUTE_TABLE_0_END = ushort(0x23FF);
    public static final int ATTRIBUTE_TABLE_0_SIZE = sint(ATTRIBUTE_TABLE_0_END) - sint(ATTRIBUTE_TABLE_0_START) + 1;

    public static final @Unsigned short LAYOUT_TABLE_1_START = ushort(0x2400);
    public static final @Unsigned short LAYOUT_TABLE_1_END = ushort(0x27BF);
    public static final int LAYOUT_TABLE_1_SIZE = sint(LAYOUT_TABLE_1_END) - sint(LAYOUT_TABLE_1_START) + 1;

    public static final @Unsigned short ATTRIBUTE_TABLE_1_START = ushort(0x27C0);
    public static final @Unsigned short ATTRIBUTE_TABLE_1_END = ushort(0x27FF);
    public static final int ATTRIBUTE_TABLE_1_SIZE = sint(ATTRIBUTE_TABLE_1_END) - sint(ATTRIBUTE_TABLE_1_START) + 1;

    public static final @Unsigned short LAYOUT_TABLE_2_START = ushort(0x2800);
    public static final @Unsigned short LAYOUT_TABLE_2_END = ushort(0x2BBF);
    public static final int LAYOUT_TABLE_2_SIZE = sint(LAYOUT_TABLE_2_END) - sint(LAYOUT_TABLE_2_START) + 1;

    public static final @Unsigned short ATTRIBUTE_TABLE_2_START = ushort(0x2BC0);
    public static final @Unsigned short ATTRIBUTE_TABLE_2_END = ushort(0x2BFF);
    public static final int ATTRIBUTE_TABLE_2_SIZE = sint(ATTRIBUTE_TABLE_2_END) - sint(ATTRIBUTE_TABLE_2_START) + 1;

    public static final @Unsigned short LAYOUT_TABLE_3_START = ushort(0x2C00);
    public static final @Unsigned short LAYOUT_TABLE_3_END = ushort(0x2FBF);
    public static final int LAYOUT_TABLE_3_SIZE = sint(LAYOUT_TABLE_3_END) - sint(LAYOUT_TABLE_3_START) + 1;

    public static final @Unsigned short ATTRIBUTE_TABLE_3_START = ushort(0x2FC0);
    public static final @Unsigned short ATTRIBUTE_TABLE_3_END = VRAM_END;
    public static final int ATTRIBUTE_TABLE_3_SIZE = sint(ATTRIBUTE_TABLE_3_END) - sint(ATTRIBUTE_TABLE_3_START) + 1;

    // endregion

    public static final @Unsigned short UNUSED_START = ushort(0x3000);
    public static final @Unsigned short UNUSED_END = ushort(0x3EFF);
    public static final int UNUSED_SIZE = sint(UNUSED_END) - sint(UNUSED_START) + 1;

    // region Internal (for CPU access)

    public static final @Unsigned short PALETTE_RAM_START = ushort(0x3F00);
    public static final @Unsigned short PALETTE_RAM_END = ushort(0x3F1F);
    public static final int PALETTE_RAM_SIZE = sint(PALETTE_RAM_END) - sint(PALETTE_RAM_START) + 1;

    public static final @Unsigned short PALETTE_RAM_MIRROR_END = ushort(0x3FFF);
    public static final int PALETTE_RAM_MIRROR_SIZE = sint(PALETTE_RAM_MIRROR_END) - sint(PALETTE_RAM_START) + 1;

    // endregion
}
