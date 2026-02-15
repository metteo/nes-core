package net.novaware.nes.core.cpu.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

/**
 * @see <a href="https://www.nesdev.org/wiki/CPU_memory_map">CPU Memory Map on nesdev.org</a>
 */
public class MemoryMap {

    public static final @Unsigned short MEMORY_START = ushort(0x0000);
    public static final @Unsigned short MEMORY_END = ushort(0xFFFF);
    public static final int MEMORY_SIZE = uint(MEMORY_END) - uint(MEMORY_START) + 1;

    public static final @Unsigned short RAM_START = ushort(0x0000);
    public static final @Unsigned short RAM_END = ushort(0x07FF);
    public static final int RAM_SIZE = uint(RAM_END) - uint(RAM_START) + 1;

    public static final @Unsigned short STACK_SEGMENT_START = ushort(0x0100);
    public static final @Unsigned short STACK_SEGMENT_END = ushort(0x01FF);
    public static final int STACK_SEGMENT_SIZE = uint(STACK_SEGMENT_END) - uint(STACK_SEGMENT_START) + 1;

    public static final @Unsigned short RAM_MIRROR_1_START = ushort(0x0800);
    public static final @Unsigned short RAM_MIRROR_1_END = ushort(0x0FFF);
    public static final int RAM_MIRROR_1_SIZE = uint(RAM_MIRROR_1_END) - uint(RAM_MIRROR_1_START) + 1;

    public static final @Unsigned short RAM_MIRROR_2_START = ushort(0x1000);
    public static final @Unsigned short RAM_MIRROR_2_END = ushort(0x17FF);
    public static final int RAM_MIRROR_2_SIZE = uint(RAM_MIRROR_2_END) - uint(RAM_MIRROR_2_START) + 1;

    public static final @Unsigned short RAM_MIRROR_3_START = ushort(0x1800);
    public static final @Unsigned short RAM_MIRROR_3_END = ushort(0x1FFF);
    public static final int RAM_MIRROR_3_SIZE = uint(RAM_MIRROR_3_END) - uint(RAM_MIRROR_3_START) + 1;

    public static final @Unsigned short PPU_REGISTERS_START = ushort(0x2000);
    public static final @Unsigned short PPU_REGISTERS_END = ushort(0x2007);
    public static final int PPU_REGISTERS_SIZE = uint(PPU_REGISTERS_END) - uint(PPU_REGISTERS_START) + 1;

    public static final @Unsigned short PPU_REGISTERS_MIRROR_START = ushort(0x2008);
    public static final @Unsigned short PPU_REGISTERS_MIRROR_END = ushort(0x3FFF);
    public static final int PPU_REGISTERS_MIRROR_SIZE = uint(PPU_REGISTERS_MIRROR_END) - uint(PPU_REGISTERS_MIRROR_START) + 1;

    public static final @Unsigned short APU_IO_REGISTERS_START = ushort(0x4000);
    public static final @Unsigned short APU_IO_REGISTERS_END = ushort(0x4017);
    public static final int APU_IO_REGISTERS_SIZE = uint(APU_IO_REGISTERS_END) - uint(APU_IO_REGISTERS_START) + 1;

    public static final @Unsigned short APU_TEST_REGISTERS_START = ushort(0x4018);
    public static final @Unsigned short APU_TEST_REGISTERS_END = ushort(0x401F);
    public static final int APU_TEST_REGISTERS_SIZE = uint(APU_TEST_REGISTERS_END) - uint(APU_TEST_REGISTERS_START) + 1;

    public static final @Unsigned short CARTRIDGE_START = ushort(0x4020);
    public static final @Unsigned short CARTRIDGE_END = ushort(0xFFFF);
    public static final int CARTRIDGE_SIZE = uint(CARTRIDGE_END) - uint(CARTRIDGE_START) + 1;

    // TODO: add subsections like ram, cartridge etc.


}
