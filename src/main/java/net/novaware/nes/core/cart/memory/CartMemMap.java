package net.novaware.nes.core.cart.memory;

import net.novaware.nes.core.memory.MemoryMap;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.USHORT_MAX_VALUE;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * Some well known Cartridge address range sections
 */
public interface CartMemMap extends MemoryMap {

    // TODO: This is CPU section, prefix names maybe?

    @Unsigned
    short MEMORY_START = ushort(0x4020);
    @Unsigned short MEMORY_END = USHORT_MAX_VALUE;
    int MEMORY_SIZE = sint(MEMORY_END) - sint(MEMORY_START) + 1;

    /**
     * @see <a href="https://www.nesdev.org/wiki/Family_Computer_Disk_System">FDS on nesdev.org</a>
     */
    @Unsigned short FDS_START = ushort(0x4020);
    @Unsigned short FDS_END = ushort(0x40FF);
    int FDS_SIZE = sint(FDS_END) - sint(FDS_START) + 1;

    @Unsigned short RAM_START = ushort(0x6000);
    @Unsigned short RAM_END = ushort(0x7FFF);
    int RAM_SIZE = sint(RAM_END) - sint(RAM_START) + 1;

    @Unsigned short ROM_START = ushort(0x8000);
    @Unsigned short ROM_END = ushort(0xFFFF);
    int ROM_SIZE = sint(ROM_END) - sint(ROM_START) + 1;

    // TODO: implement PPU section with prefixes or do subinterfaces
}
