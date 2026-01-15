package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.MagicNumber;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.NesMeta.Kind;
import net.novaware.nes.core.file.NesMeta.Layout;
import net.novaware.nes.core.file.NesMeta.VideoStandard;
import net.novaware.nes.core.util.Quantity;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Asserts.assertState;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_512B;
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

    public static class Archaic_iNES {

        // region Bytes 0-3

        public static final int  BYTE_0 = 0;
        public static final MagicNumber MAGIC_NUMBER = MagicNumber.GAME_NES;

        // endregion
        // region Bytes 4-5

        public static final int  BYTE_4 = 4;
        public static final byte PROGRAM_DATA_SIZE = ubyte(0xFF);

        public static final int  BYTE_5 = 5;
        public static final byte VIDEO_DATA_SIZE   = ubyte(0xFF);

        // endregion
        // region Byte 6

        public static final int  BYTE_6 = 6;
        public static final byte MAPPER_LO_BITS = ubyte(0b1111_0000);
        public static final byte LAYOUT_BITS    = ubyte(0b0000_1001);
        public static final byte TRAINER_BIT    = ubyte(0b0000_0100);
        public static final byte BATTERY_BIT    = ubyte(0b0000_0010);

        public record Byte6(short mapper, Layout layout, Quantity trainer, Kind kind){}

        // endregion

        public static ByteBuffer putMagic(ByteBuffer header) {
            assertState(header.position() == 0, "buffer not at position 0");

            return header.put(MAGIC_NUMBER.numbers());
        }

        public static byte[] getMagic(ByteBuffer headerBuffer) {
            assertState(headerBuffer.position() == BYTE_0, "buffer not at position 0");
            byte[] fourBytes = new byte[4];
            headerBuffer.get(fourBytes);

            return fourBytes;
        }

        public static ByteBuffer putProgramData(ByteBuffer header, Quantity programData) {
            assertState(header.position() == BYTE_4, "buffer not at position 4");
            assertArgument(programData.unit() == BANK_16KB, "program data size not in 16KB units");
            assertArgument(programData.amount() <= uint(PROGRAM_DATA_SIZE), "program data size exceeded");

            return header.put(ubyte(programData.amount()));
        }
        
        public static Quantity getProgramData(ByteBuffer header) {
            assertState(header.position() == BYTE_4, "buffer not at position 4");
            
            byte byte4 = header.get();
            return new Quantity(uint(byte4), BANK_16KB);
        }

        public static ByteBuffer putVideoData(ByteBuffer header, Quantity videoData) {
            assertState(header.position() == BYTE_5, "buffer not at position 5");
            assertArgument(videoData.unit() == BANK_8KB, "video data size not in 8KB units");
            assertArgument(videoData.amount() <= uint(VIDEO_DATA_SIZE), "video data size exceeded");

            return header.put(ubyte(videoData.amount()));
        }
        
        public static Quantity getVideoData(ByteBuffer header) {
            assertState(header.position() == BYTE_5, "buffer not at position 5");

            byte byte5 = header.get();
            return new Quantity(uint(byte5), BANK_8KB);
        }

        public static ByteBuffer putByte6(ByteBuffer header, NesMeta meta) {
            return putByte6(header, new Byte6(
                    meta.mapper(),
                    meta.videoData().layout(),
                    meta.trainer(),
                    meta.programMemory().kind()
            ));
        }

        public static ByteBuffer putByte6(ByteBuffer header, Byte6 meta) {
            assertState(header.position() == BYTE_6, "buffer not at position 6");

            Quantity trainer = meta.trainer();
            assertArgument(trainer.unit() == BANK_512B, "trainer size not in 512KB units");
            assertArgument(trainer.amount() <= 1, "trainer size exceeded");

            int mapperLoBits = (uint(meta.mapper()) & 0x0F) << 4;
            int layoutBits = meta.layout().bits();
            int trainerBit = meta.trainer().amount() == 1 ? uint(TRAINER_BIT) : 0;
            int batteryBit = meta.kind() == Kind.PERSISTENT ? uint(BATTERY_BIT) : 0;

            byte flag6 = ubyte(mapperLoBits | layoutBits | trainerBit | batteryBit);

            return header.put(flag6);
        }

        static Byte6 getByte6(ByteBuffer header) {
            assertState(header.position() == BYTE_6, "buffer not at position 6");

            int byte6 = uint(header.get());
            int mapperBits = (byte6 & uint(MAPPER_LO_BITS)) >> 4;
            int layoutBits = (byte6 & uint(LAYOUT_BITS));
            int trainerBit = (byte6 & uint(TRAINER_BIT)) >> 2;
            int batteryBit = (byte6 & uint(BATTERY_BIT)) >> 1;

            return new Byte6(
                (short) mapperBits,
                Layout.fromBits(layoutBits),
                new Quantity(trainerBit, BANK_512B),
                batteryBit == 0 ? Kind.VOLATILE : Kind.PERSISTENT
            );
        }

        public static ByteBuffer putInfo(ByteBuffer header, String info) {
            return header;
        }
    }

    public static class Shared_iNES extends Archaic_iNES {

        // region Byte 7

        public static final int  BYTE_7           = 7;
        public static final byte MAPPER_HI_BITS   = ubyte(0b1111_0000);
        public static final byte VERSION_BITS     = ubyte(0b0000_1100);
        public static final byte SYSTEM_TYPE_BITS = ubyte(0b0000_0011);

        public record Byte7(short mapperHi, byte versionBits, byte systemTypeBits) {}

        // endregion

        public static ByteBuffer putByte7(ByteBuffer header, NesMeta meta, Version version) {
            return putByte7(header, meta.system(), meta.mapper(), version);
        }

        // TODO: flag7 is system specific as it turns out,
        //  should be created from scratch for NES 2.0
        //  archaic nes doesn't support this
        //  nes 0.7 only mapper hi bits
        public static ByteBuffer putByte7(ByteBuffer header, NesMeta.System system, short mapper, Version version) {
            assertState(header.position() == BYTE_7, "buffer not at position 7");
            // TODO: better assertions

            int systemBits = system.bits();
            int versionBits = 0;
            int mapperHiBits = uint(mapper) & 0xF0;

            byte flags7 = ubyte(mapperHiBits | versionBits | systemBits);

            return header.put(flags7);
        }

        public static Byte7 getByte7(ByteBuffer header) {
            assertState(header.position() == BYTE_7, "buffer not at position 7");

            int byte7 = uint(header.get());

            int mapperHiBits = (byte7 & uint(MAPPER_HI_BITS));
            int versionBits = (byte7 & uint(VERSION_BITS)) >> 2;
            int systemTypeBits = byte7 & uint(SYSTEM_TYPE_BITS);

            return new Byte7(
                    (short) mapperHiBits,
                    ubyte(versionBits),
                    ubyte(systemTypeBits)
            );
        }
    }

    public static class Modern_iNES extends Shared_iNES {

        // region Byte 8

        public static final int  BYTE_8              = 8;
        public static final byte PROGRAM_MEMORY_SIZE = ubyte(0xFF);

        // endregion
        // region Byte 9

        public static final int  BYTE_9               = 9;
        public static final byte BYTE_9_RESERVED_BITS = ubyte(0b1111_1110);
        public static final byte VIDEO_STANDARD_BITS  = ubyte(0b0000_0001);

        // endregion

        public static ByteBuffer putProgramMemory(ByteBuffer header, Quantity programMemory) {
            assertState(header.position() == 8, "buffer not at position 8");
            assertArgument(programMemory.unit() == BANK_8KB, "program memory size not in 8KB units");
            assertArgument(programMemory.amount() <= uint(PROGRAM_MEMORY_SIZE), "program memory size exceeded");

            byte byte8 = ubyte(programMemory.amount());

            return header.put(byte8);
        }

        public static Quantity getProgramMemory(ByteBuffer header) {
            assertState(header.position() == BYTE_8, "buffer not at position 8");

            int byte8 = uint(header.get());
            // TODO: consider moving this up one level and report as problem to fix instead of defaulting to 1
            int amount = Math.max(byte8, 1); // Value 0 infers 8 KB for compatibility

            return new Quantity(amount, BANK_8KB);
        }

        public static ByteBuffer putVideoStandard(ByteBuffer header, VideoStandard videoStandard) {
            assertState(header.position() == 9, "buffer not at position 9");

            // TODO: what about dual standard games?
            byte byte9 = videoStandard == VideoStandard.PAL ? ubyte(1) : ubyte(0);

            return header.put(byte9);
        }

        public static VideoStandard getVideoStandard(ByteBuffer header) {
            assertState(header.position() == BYTE_9, "buffer not at position 9");

            int byte9 = uint(header.get());

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

        public static final int  BYTE_10                    = 10;
        public static final byte BYTE_10_RESERVED_BITS      = ubyte(0b1100_1100);
        public static final byte BUS_CONFLICTS_BIT          = ubyte(0b0010_0000);
        public static final byte PROGRAM_MEMORY_PRESENT_BIT = ubyte(0b0001_0000);
        public static final byte VIDEO_STANDARD_2_BITS      = ubyte(0b0000_0011);

        public record Byte10(boolean busConflicts, boolean programMemoryPresent, VideoStandard videoStandard) {
        }

        public static ByteBuffer putByte10(ByteBuffer header, Byte10 byte10) {
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

            byte flag10 = ubyte(busConflictsBit | programMemoryPresentBit | videoStandardBits);

            return header.put(flag10);
        }

        public static Byte10 getByte10(ByteBuffer header) {
            assertState(header.position() == BYTE_10, "buffer not at position 10");

            int byte10 = uint(header.get());

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
