package net.novaware.nes.core.easy.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

public class EasyMemMap {

    public static final @Unsigned short MEMORY_START = ushort(0x0000);
    public static final @Unsigned short MEMORY_END = ushort(0xFFFF);
    public static final int MEMORY_SIZE = sint(MEMORY_END) - sint(MEMORY_START) + 1;

    public static final @Unsigned short RAM_START = ushort(0x0000);
    public static final @Unsigned short RAM_END = ushort(0x00FF);
    public static final int RAM_SIZE = sint(RAM_END) - sint(RAM_START) + 1;

    public static final @Unsigned short RNG_BYTE = ushort(0x00FE);
    public static final @Unsigned short JOY_BYTE = ushort(0x00FF);

    public static final @Unsigned short STACK_SEGMENT_START = ushort(0x0100);
    public static final @Unsigned short STACK_SEGMENT_END = ushort(0x01FF);
    public static final int STACK_SEGMENT_SIZE = sint(STACK_SEGMENT_END) - sint(STACK_SEGMENT_START) + 1;

    public static final @Unsigned short PICTURE_SEGMENT_START = ushort(0x0200);
    public static final @Unsigned short PICTURE_SEGMENT_END = ushort(0x05FF);
    public static final int PICTURE_SEGMENT_SIZE = sint(PICTURE_SEGMENT_END) - sint(PICTURE_SEGMENT_START) + 1;

    public static final @Unsigned short CARTRIDGE_START = ushort(0x0600);
    public static final @Unsigned short CARTRIDGE_END = ushort(0xFFFF);
    public static final int CARTRIDGE_SIZE = sint(CARTRIDGE_END) - sint(CARTRIDGE_START) + 1;

    public static final @Unsigned short NMI_VECTOR = ushort(0xFFFA);
    public static final @Unsigned short RES_VECTOR = ushort(0xFFFC);
    public static final @Unsigned short IRQ_VECTOR = ushort(0xFFFE);
}
