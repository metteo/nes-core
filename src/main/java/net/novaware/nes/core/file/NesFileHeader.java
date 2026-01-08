package net.novaware.nes.core.file;

import net.novaware.nes.core.file.NesFile.Meta;
import net.novaware.nes.core.util.Quantity;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

/**
 * Bits, Bytes and Logical Ops off the header
 *
 * <a href="https://www.nesdev.org/wiki/INES">iNES on nesdev.org</a>
 * <a href="https://www.nesdev.org/wiki/NES_2.0">NES 2.0 on nesdev.org</a>
 */
public class NesFileHeader {

    public static class Archaic_iNES {

        // region Bytes 0-3

        public static final byte LETTER_N = 0x4E;
        public static final byte LETTER_E = 0x45;
        public static final byte LETTER_S = 0x53;
        public static final byte EOF      = 0x1A;

        // endregion
        // region Bytes 4-5

        public static final byte PROGRAM_DATA_SIZE = ubyte(0xFF);
        public static final byte VIDEO_DATA_SIZE   = ubyte(0xFF);

        // endregion
        // region Byte 6

        public static final byte MAPPER_LO_BITS = ubyte(0b1111_0000);
        public static final byte MIRRORING_BITS = ubyte(0b0000_1001);
        public static final byte TRAINER_BIT    = ubyte(0b0000_0100);
        public static final byte BATTERY_BIT    = ubyte(0b0000_0010);

        // endregion

        public static ByteBuffer putMagic(ByteBuffer header) {
            assertState(header.position() == 0, "buffer not at position 0");

            return header.put(new byte[] { LETTER_N, LETTER_E, LETTER_S, EOF });
        }

        public static ByteBuffer putProgramData(ByteBuffer header, Quantity programData) {
            assertState(header.position() == 4, "buffer not at position 4");
            assertArgument(programData.amount() <= uint(PROGRAM_DATA_SIZE), "program data size exceeded");
            assertArgument(programData.unit() == Quantity.Unit.BANK_16KB, "program data size not in 16KB units");

            return header.put(ubyte(programData.amount()));
        }

        public static ByteBuffer putVideoData(ByteBuffer header, Quantity videoData) {
            assertState(header.position() == 5, "buffer not at position 5");
            assertArgument(videoData.amount() <= uint(VIDEO_DATA_SIZE), "video data size exceeded");
            assertArgument(videoData.unit() == Quantity.Unit.BANK_8KB, "video data size not in 8KB units");

            return header.put(ubyte(videoData.amount()));
        }

        public static ByteBuffer putByte6(ByteBuffer header, Meta meta) {
            assertState(header.position() == 6, "buffer not at position 6");

            // FIXME: continue here!:


            int mapper = (uint(meta.mapper()) | 0x0F) << 4; // TODO: put only the low bits,
            int mirroring = meta.layout().ordinal(); // TODO: construct the bits properly
            int trainer = meta.trainer().amount(); // TODO: check unit, then translate
            int battery = meta.programMemory().kind() == NesFile.Kind.PERSISTENT ? uint(BATTERY_BIT) : 0;

            byte byte6 = ubyte(mapper | mirroring | trainer | battery);

            return header.put(byte6);
        }
    }

    public static class Shared_iNES extends Archaic_iNES {

        // region Byte 7

        public static final byte MAPPER_HI_BITS   = ubyte(0b1111_0000);
        public static final byte NES_2_0_BITS     = ubyte(0b0000_1100);
        public static final byte SYSTEM_TYPE_BITS = ubyte(0b0000_0011);

        // endregion

    }

    public static class Modern_iNES extends Shared_iNES {

        // region Byte 8

        public static final byte PROGRAM_MEMORY_SIZE = ubyte(0xFF);

        // endregion
        // region Byte 9

        public static final byte BYTE_9_RESERVED_BITS = ubyte(0b1111_1110);
        public static final byte VIDEO_STANDARD_BITS  = ubyte(0b0000_0001);

        // endregion
        // region Byte 10

        public static final byte BYTE_10_RESERVED_BITS      = ubyte(0b1100_1100);
        public static final byte PROGRAM_MEMORY_PRESENT_BIT = ubyte(0b0011_0000);
        public static final byte VIDEO_STANDARD_2_BITS      = ubyte(0b0000_0011);

        // endregion
    }

    public static class NES_2_0 extends Shared_iNES {

        // region Byte 8

        public static final byte MAPPER_MSB_BITS  = ubyte(0b0000_1111);
        public static final byte SUB_MAPPER_BITS  = ubyte(0b1111_0000);

        // endregion
        // region Byte 9

        public static final byte VIDEO_DATA_SIZE_MSB_BITS   = ubyte(0b1111_0000);
        public static final byte PROGRAM_DATA_SIZE_MSB_BITS = ubyte(0b0000_1111);

        // endregion
        // region Byte 10

        public static final byte PROGRAM_STORAGE_SIZE_SHIFT_BITS  = ubyte(0b1111_0000);
        public static final byte PROGRAM_MEMORY_SIZE_SHIFT_BITS   = ubyte(0b0000_1111);

        // endregion
        // region Byte 11

        public static final byte VIDEO_STORAGE_SIZE_SHIFT_BITS  = ubyte(0b1111_0000);
        public static final byte VIDEO_MEMORY_SIZE_SHIFT_BITS   = ubyte(0b0000_1111);

        // endregion
        // region Byte 12

        public static final byte BYTE_12_RESERVED_BITS = ubyte(0b1111_1100);
        public static final byte VIDEO_STANDARD_BITS   = ubyte(0b0000_0011);

        // endregion
        // region Byte 13A VS.System details

        public static final byte VS_SYSTEM_SUBTYPE_BITS = ubyte(0b1111_0000);
        public static final byte VS_SYSTEM_VIDEO_BITS   = ubyte(0b0000_1111);

        // endregion
        // region Byte 13B Ext. Console details

        public static final byte BYTE_13B_RESERVED_BITS   = ubyte(0b1111_0000);
        public static final byte EXT_CONSOLE_SUBTYPE_BITS = ubyte(0b0000_1111);

        // endregion
        // region Byte 14

        public static final byte BYTE_14_RESERVED_BITS = ubyte(0b1111_1100);
        public static final byte MISC_ROMS_BITS        = ubyte(0b0000_0011);

        // endregion
        // region Byte 15

        public static final byte BYTE_15_RESERVED_BITS         = ubyte(0b1100_0000);
        public static final byte DEFAULT_EXPANSION_DEVICE_BITS = ubyte(0b0011_1111);

        // endregion
    }
}
