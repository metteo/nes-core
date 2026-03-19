package net.novaware.nes.core.cpu.memory;

import net.novaware.nes.core.memory.MemoryMap;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.USHORT_0;
import static net.novaware.nes.core.util.UTypes.USHORT_MAX_VALUE;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/CPU_memory_map">CPU Memory Map on nesdev.org</a>
 */
public interface CpuMemMap extends MemoryMap {

    @Unsigned short MEMORY_START = USHORT_0;
    @Unsigned short MEMORY_END = USHORT_MAX_VALUE;
    int MEMORY_SIZE = sint(MEMORY_END) - sint(MEMORY_START) + 1;

    // region RAM

    @Unsigned short RAM_START = ushort(0x0000);
    @Unsigned short RAM_END = ushort(0x07FF);
    int RAM_SIZE = sint(RAM_END) - sint(RAM_START) + 1;

    @Unsigned short RAM_MIRROR_END = ushort(0x1FFF);
    int RAM_MIRROR_SIZE = sint(RAM_MIRROR_END) - sint(RAM_START) + 1;

    // endregion
    // region RAM Segments

    @Unsigned short ZERO_PAGE_START = ushort(0x0000);
    @Unsigned short ZERO_PAGE_END = ushort(0x00FF);
    int ZERO_PAGE_SIZE = sint(ZERO_PAGE_END) - sint(ZERO_PAGE_START) + 1;

    @Unsigned short STACK_SEGMENT_START = ushort(0x0100);
    @Unsigned short STACK_SEGMENT_END = ushort(0x01FF);
    int STACK_SEGMENT_SIZE = sint(STACK_SEGMENT_END) - sint(STACK_SEGMENT_START) + 1;

    @Unsigned short OAM_SEGMENT_START = ushort(0x0200); // usual location
    @Unsigned short OAM_SEGMENT_END = ushort(0x02FF);
    int OAM_SEGMENT_SIZE = sint(OAM_SEGMENT_END) - sint(OAM_SEGMENT_START) + 1;

    // endregion
    // region PPU

    @Unsigned short PPU_REGISTERS_START = ushort(0x2000);
    @Unsigned short PPU_REGISTERS_END = ushort(0x2007);
    int PPU_REGISTERS_SIZE = sint(PPU_REGISTERS_END) - sint(PPU_REGISTERS_START) + 1;

    @Unsigned short PPU_REGISTERS_MIRROR_END = ushort(0x3FFF);
    int PPU_REGISTERS_MIRROR_SIZE = sint(PPU_REGISTERS_MIRROR_END) - sint(PPU_REGISTERS_START) + 1;

    // endregion
    // region APU / IO / Test / Timer / Cartridge FDS

    @Unsigned short APU_REGISTERS_START = ushort(0x4000);
    @Unsigned short APU_REGISTERS_END = ushort(0x4013);
    int APU_REGISTERS_SIZE = sint(APU_REGISTERS_END) - sint(APU_REGISTERS_START) + 1;

    @Unsigned short IO_REGISTERS_START = ushort(0x4014);
    @Unsigned short IO_REGISTERS_END = ushort(0x4017);
    int IO_REGISTERS_SIZE = sint(IO_REGISTERS_END) - sint(IO_REGISTERS_START) + 1;

    @Unsigned short APU_TEST_REGISTERS_START = ushort(0x4018);
    @Unsigned short APU_TEST_REGISTERS_END = ushort(0x401B);
    int APU_TEST_REGISTERS_SIZE = sint(APU_TEST_REGISTERS_END) - sint(APU_TEST_REGISTERS_START) + 1;

    @Unsigned short TIMER_REGISTERS_START = ushort(0x401C);
    @Unsigned short TIMER_REGISTERS_END = ushort(0x401F);
    int TIMER_REGISTERS_SIZE = sint(TIMER_REGISTERS_END) - sint(TIMER_REGISTERS_START) + 1;

    @Unsigned short CARTRIDGE_FDS_START = ushort(0x4020);
    @Unsigned short CARTRIDGE_FDS_END = ushort(0x40FF);
    int CARTRIDGE_FDS_SIZE = sint(CARTRIDGE_FDS_END) - sint(CARTRIDGE_FDS_START) + 1;

    // endregion
    // region Cartridge

    @Unsigned short CARTRIDGE_START = ushort(0x4100);
    @Unsigned short CARTRIDGE_END = ushort(0xFFFF);
    int CARTRIDGE_SIZE = sint(CARTRIDGE_END) - sint(CARTRIDGE_START) + 1;

    // endregion
    // region Cartridge Segments

    @Unsigned short CARTRIDGE_RAM_START = ushort(0x6000);
    @Unsigned short CARTRIDGE_RAM_END = ushort(0x7FFF);
    int CARTRIDGE_RAM_SIZE = sint(CARTRIDGE_RAM_END) - sint(CARTRIDGE_RAM_START) + 1;

    @Unsigned short CARTRIDGE_ROM_START = ushort(0x8000);
    @Unsigned short CARTRIDGE_ROM_END = ushort(0xFFFF);
    int CARTRIDGE_ROM_SIZE = sint(CARTRIDGE_ROM_END) - sint(CARTRIDGE_ROM_START) + 1;

    // endregion
    // region Vectors

    @Unsigned short NMI_VECTOR = ushort(0xFFFA);
    @Unsigned short RES_VECTOR = ushort(0xFFFC);
    @Unsigned short IRQ_VECTOR = ushort(0xFFFE);

    // endregion
}
