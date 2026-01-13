package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.MagicNumber;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.NesMeta.Kind;
import net.novaware.nes.core.file.NesMeta.Layout;
import net.novaware.nes.core.file.NesMeta.ProgramMemory;
import net.novaware.nes.core.file.NesMeta.VideoStandard;
import net.novaware.nes.core.util.Quantity;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.file.NesMeta.System.EXTENDED;
import static net.novaware.nes.core.file.NesMeta.System.NES;
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

        public static ByteBuffer putFlag7(ByteBuffer header, NesMeta meta, Version version) {
            assertState(header.position() == 7, "buffer not at position 7");
            // TODO: better assertions

            final NesMeta.System system = meta.system();
            final NesMeta.System versionAwareSystem = version.compareTo(Version.NES_2_0) < 0 && system == EXTENDED
                    ? NES // default to NES for older versions
                    : system;

            int systemBits = versionAwareSystem.bits();
            int nes20 = version == Version.NES_2_0 ? 0b10 : 0; // TODO: should follow detection procedure from reader
            int mapperHiBits = (uint(meta.mapper()) & 0xF0);

            byte flags7 = ubyte(mapperHiBits | nes20 | systemBits);

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

        public static final byte PROGRAM_MEMORY_SIZE = ubyte(0xFF);

        // endregion
        // region Byte 9

        public static final byte BYTE_9_RESERVED_BITS = ubyte(0b1111_1110);
        public static final byte VIDEO_STANDARD_BITS  = ubyte(0b0000_0001);

        // endregion

        /** flag8 */
        public static ByteBuffer putProgramMemory(ByteBuffer header, ProgramMemory programMemory) {
            assertState(header.position() == 8, "buffer not at position 8");

            final Quantity size = programMemory.size();

            switch (programMemory.kind()) {
                case PERSISTENT:
                case VOLATILE:
                case UNKNOWN: // we don't know if volatile or persistent, but we know the size
                    assertArgument(size.unit() == BANK_8KB, "program memory size not in 8KB units");
                    assertArgument(size.amount() <= uint(PROGRAM_MEMORY_SIZE), "program memory size exceeded");
                    break;
                case NONE:
                default:
                    assertArgument(size.amount() == 0, "program memory size should be 0");
                    break;
            }

            byte flag8 = ubyte(size.amount());

            return header.put(flag8);
        }

        /** flag9 */
        public static ByteBuffer putVideoStandard(ByteBuffer header, VideoStandard videoStandard) {
            assertState(header.position() == 9, "buffer not at position 9");

            // TODO: what about dual standard games?
            byte flag9 = videoStandard == NesMeta.VideoStandard.PAL ? ubyte(1) : ubyte(0);

            return header.put(flag9);
        }
    }

    public static class Unofficial_iNES extends Modern_iNES {

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
