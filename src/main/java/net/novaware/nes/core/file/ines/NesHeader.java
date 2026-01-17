package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.NesMeta.VideoStandard;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

/**
 * Bits, Bytes and Logical Ops off the header
 *
 * <a href="https://www.nesdev.org/wiki/INES">iNES on nesdev.org</a>
 * <a href="https://www.nesdev.org/wiki/NES_2.0">NES 2.0 on nesdev.org</a>
 */
public class NesHeader {

    public static final int SIZE = 16; // bytes

    public enum Version {
        // TODO: add field with ordering and methods to compare versions. somehow allow to derive one version from another?
        ARCHAIC_iNES,

        NES_0_7,

        MODERN_iNES,

        UNOFFICIAL_iNES, // flag 10: tv-system pal/ntsc/dual, prg-ram present/not, board conflicts: yes/no

        NES_2_0
    }

    public static class Modern_iNES {

        // region Byte 8

        public static final int BYTE_8 = 8;
        public static final @Unsigned byte PROGRAM_MEMORY_SIZE = ubyte(0xFF);

        // endregion
        // region Byte 9

        public static final int BYTE_9 = 9;
        public static final @Unsigned byte BYTE_9_RESERVED_BITS = ubyte(0b1111_1110);
        public static final @Unsigned byte VIDEO_STANDARD_BITS  = ubyte(0b0000_0001);

        // endregion

        public static UByteBuffer putProgramMemory(UByteBuffer header, Quantity programMemory) {
            assertState(header.position() == 8, "buffer not at position 8");
            assertArgument(programMemory.unit() == BANK_8KB, "program memory size not in 8KB units");
            assertArgument(programMemory.amount() <= uint(PROGRAM_MEMORY_SIZE), "program memory size exceeded");

            int byte8 = programMemory.amount();

            return header.putAsByte(byte8);
        }

        public static Quantity getProgramMemory(UByteBuffer header) {
            assertState(header.position() == BYTE_8, "buffer not at position 8");

            int byte8 = header.getAsInt();

            return new Quantity(byte8, BANK_8KB);
        }

        public static UByteBuffer putVideoStandard(UByteBuffer header, VideoStandard videoStandard) {
            assertState(header.position() == 9, "buffer not at position 9");

            // TODO: what about dual standard games?
            int byte9 = videoStandard == VideoStandard.PAL ? 1 : 0;

            return header.putAsByte(byte9);
        }

        public static VideoStandard getVideoStandard(UByteBuffer header) {
            assertState(header.position() == BYTE_9, "buffer not at position 9");

            int byte9 = header.getAsInt();

            int reservedBits = (byte9 & uint(BYTE_9_RESERVED_BITS)) >> 1;
            VideoStandard videoStandard = (byte9 & uint(VIDEO_STANDARD_BITS)) == 1
                    ? VideoStandard.PAL
                    : VideoStandard.NTSC;

            assertState(reservedBits == 0, "reserved bits not 0"); // TODO: maybe report as Problem?

            return videoStandard;
        }
    }

    public static class Unofficial_iNES extends Modern_iNES {

        // region Byte 10

        public static final int BYTE_10 = 10;
        public static final @Unsigned byte BYTE_10_RESERVED_BITS      = ubyte(0b1100_1100);
        public static final @Unsigned byte BUS_CONFLICTS_BIT          = ubyte(0b0010_0000);
        public static final @Unsigned byte PROGRAM_MEMORY_PRESENT_BIT = ubyte(0b0001_0000);
        public static final @Unsigned byte VIDEO_STANDARD_2_BITS      = ubyte(0b0000_0011);

        public record Byte10(boolean busConflicts, boolean programMemoryPresent, VideoStandard videoStandard) {
        }

        public static UByteBuffer putByte10(UByteBuffer header, Byte10 byte10) {
            assertState(header.position() == BYTE_10, "buffer not at position 10");

            int busConflictsBit = byte10.busConflicts() ? uint(BUS_CONFLICTS_BIT) : 0;
            int programMemoryPresentBit = byte10.programMemoryPresent() ? uint(PROGRAM_MEMORY_PRESENT_BIT) : 0;
            int videoStandardBits = switch (byte10.videoStandard()) {
                case NTSC -> 0;
                case NTSC_HYBRID -> 1;
                case PAL -> 2;
                case PAL_HYBRID -> 3;
                default -> {
                    assertArgument(false, "Unsupported video standard for Unofficial_iNES byte 10: " + byte10.videoStandard());
                    yield 0; // Should not be reached due to assertion
                }
            };

            int flag10 = busConflictsBit | programMemoryPresentBit | videoStandardBits;

            return header.putAsByte(flag10);
        }

        public static Byte10 getByte10(UByteBuffer header) {
            assertState(header.position() == BYTE_10, "buffer not at position 10");

            int byte10 = header.getAsInt();

            int reservedBits = (byte10 & uint(BYTE_10_RESERVED_BITS));
            int busConflictsBit = (byte10 & uint(BUS_CONFLICTS_BIT)) >> 5;
            int programMemoryPresentBit = (byte10 & uint(PROGRAM_MEMORY_PRESENT_BIT)) >> 4;
            int videoStandardBits = byte10 & uint(VIDEO_STANDARD_2_BITS);

            assertState(reservedBits == 0, "reserved bits not 0"); // TODO: maybe report as Problem?

            NesMeta.VideoStandard videoStandard = switch (videoStandardBits) {
                case 0 -> NesMeta.VideoStandard.NTSC;
                case 1 -> NesMeta.VideoStandard.NTSC_HYBRID;
                case 2 -> NesMeta.VideoStandard.PAL;
                case 3 -> NesMeta.VideoStandard.PAL_HYBRID; // TODO: Dendy?
                default -> NesMeta.VideoStandard.OTHER; // TODO: report a problem
            };

            boolean busConflicts = busConflictsBit == 1;
            boolean programMemoryPresent = programMemoryPresentBit == 1; // TODO: default to true?

            return new Byte10(busConflicts, programMemoryPresent, videoStandard);
        }
        // endregion
    }

    public static class NES_2_0 {

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
    }
}
