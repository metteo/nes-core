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

    @Unsigned short RAM_START = USHORT_0;
    @Unsigned short RAM_END = ushort(0x07FF);
    int RAM_SIZE = sint(RAM_END) - sint(RAM_START) + 1;

    @Unsigned short RAM_MIRROR_END = ushort(0x1FFF);
    int RAM_MIRROR_SIZE = sint(RAM_MIRROR_END) - sint(RAM_START) + 1;

    // endregion
    // region RAM Segments

    @Unsigned short ZERO_PAGE_START = USHORT_0;
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

    @Unsigned short PPU_CONTROL_REGISTER     = ushort(0x2000);
    @Unsigned short PPU_MASK_REGISTER        = ushort(0x2001);
    @Unsigned short PPU_STATUS_REGISTER      = ushort(0x2002);
    @Unsigned short PPU_OAM_ADDRESS_REGISTER = ushort(0x2003);
    @Unsigned short PPU_OAM_DATA_REGISTER    = ushort(0x2004);
    @Unsigned short PPU_SCROLL_REGISTER      = ushort(0x2005);
    @Unsigned short PPU_BUS_ADDRESS_REGISTER = ushort(0x2006);
    @Unsigned short PPU_BUS_DATA_REGISTER    = ushort(0x2007);

    @Unsigned short PPU_REGISTERS_END = ushort(0x2007);
    int PPU_REGISTERS_SIZE = sint(PPU_REGISTERS_END) - sint(PPU_REGISTERS_START) + 1;

    @Unsigned short PPU_REGISTERS_MIRROR_END = ushort(0x3FFF);
    int PPU_REGISTERS_MIRROR_SIZE = sint(PPU_REGISTERS_MIRROR_END) - sint(PPU_REGISTERS_START) + 1;

    // endregion
    // region APU / OAM DMA / IO / Test / Timer

    @Unsigned short APU_REGISTERS_START = ushort(0x4000);
    @Unsigned short APU_REGISTERS_END = ushort(0x4013);
    int APU_REGISTERS_SIZE = sint(APU_REGISTERS_END) - sint(APU_REGISTERS_START) + 1;

    @Unsigned short OAM_DMA_REGISTER = ushort(0x4014);
    @Unsigned short APU_STATUS_REGISTER = ushort(0x4015);

    // TODO: rename to JOY_... ?
    @Unsigned short IO_REGISTERS_START = ushort(0x4016);
    @Unsigned short IO_REGISTERS_END = ushort(0x4017);
    int IO_REGISTERS_SIZE = sint(IO_REGISTERS_END) - sint(IO_REGISTERS_START) + 1;

    @Unsigned short APU_TEST_REGISTERS_START = ushort(0x4018);
    @Unsigned short APU_TEST_REGISTERS_END = ushort(0x401B);
    int APU_TEST_REGISTERS_SIZE = sint(APU_TEST_REGISTERS_END) - sint(APU_TEST_REGISTERS_START) + 1;

    @Unsigned short TIMER_REGISTERS_START = ushort(0x401C);
    @Unsigned short TIMER_REGISTERS_END = ushort(0x401F);
    int TIMER_REGISTERS_SIZE = sint(TIMER_REGISTERS_END) - sint(TIMER_REGISTERS_START) + 1;

    // endregion
    // region Cartridge

    @Unsigned short CARTRIDGE_START = ushort(0x4020);
    @Unsigned short CARTRIDGE_END = USHORT_MAX_VALUE;
    int CARTRIDGE_SIZE = sint(CARTRIDGE_END) - sint(CARTRIDGE_START) + 1;

    // endregion
    // region Vectors

    @Unsigned short NMI_VECTOR = ushort(0xFFFA);
    @Unsigned short RES_VECTOR = ushort(0xFFFC);
    @Unsigned short IRQ_VECTOR = ushort(0xFFFE);

    // endregion
}
