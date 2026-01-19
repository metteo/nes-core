package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;

/**
 * NES 2.0 compatible header buffer
 * <p>
 * <a href="https://www.nesdev.org/wiki/NES_2.0">NES 2.0 on nesdev.org</a>
 */
public class FutureHeaderBuffer extends BaseHeaderBuffer {

    // region Byte 7

    public static final int  BYTE_7 = 7;
    public static final @Unsigned byte MAPPER_HI_BITS   = ubyte(0b1111_0000);
    public static final @Unsigned byte VERSION_BITS     = ubyte(0b0000_1100);
    public static final @Unsigned byte SYSTEM_TYPE_BITS = ubyte(0b0000_0011);

    // endregion
    // region Byte 8

    public static final @Unsigned byte MAPPER_MSB_BITS  = ubyte(0b0000_1111);
    public static final @Unsigned byte SUB_MAPPER_BITS  = ubyte(0b1111_0000);

    // endregion
    // region Byte 9

    public static final @Unsigned byte VIDEO_DATA_SIZE_MSB_BITS   = ubyte(0b1111_0000);
    public static final @Unsigned byte PROGRAM_DATA_SIZE_MSB_BITS = ubyte(0b0000_1111);

    // endregion
    // region Byte 10

    public static final @Unsigned byte PROGRAM_STORAGE_SIZE_SHIFT_BITS  = ubyte(0b1111_0000);
    public static final @Unsigned byte PROGRAM_MEMORY_SIZE_SHIFT_BITS   = ubyte(0b0000_1111);

    // endregion
    // region Byte 11

    public static final @Unsigned byte VIDEO_STORAGE_SIZE_SHIFT_BITS  = ubyte(0b1111_0000);
    public static final @Unsigned byte VIDEO_MEMORY_SIZE_SHIFT_BITS   = ubyte(0b0000_1111);

    // endregion
    // region Byte 12

    public static final @Unsigned byte BYTE_12_RESERVED_BITS = ubyte(0b1111_1100);
    public static final @Unsigned byte VIDEO_STANDARD_BITS   = ubyte(0b0000_0011);

    // endregion
    // region Byte 13A VS.System details

    public static final @Unsigned byte VS_SYSTEM_SUBTYPE_BITS = ubyte(0b1111_0000);
    public static final @Unsigned byte VS_SYSTEM_VIDEO_BITS   = ubyte(0b0000_1111);

    // endregion
    // region Byte 13B Ext. Console details

    public static final @Unsigned byte BYTE_13B_RESERVED_BITS   = ubyte(0b1111_0000);
    public static final @Unsigned byte EXT_CONSOLE_SUBTYPE_BITS = ubyte(0b0000_1111);

    // endregion
    // region Byte 14

    public static final @Unsigned byte BYTE_14_RESERVED_BITS = ubyte(0b1111_1100);
    public static final @Unsigned byte MISC_ROMS_BITS        = ubyte(0b0000_0011);

    // endregion
    // region Byte 15

    public static final @Unsigned byte BYTE_15_RESERVED_BITS         = ubyte(0b1100_0000);
    public static final @Unsigned byte DEFAULT_EXPANSION_DEVICE_BITS = ubyte(0b0011_1111);

    // endregion

    public FutureHeaderBuffer(UByteBuffer header) {
        super(header);
    }
}
