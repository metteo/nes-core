package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.MagicNumber;
import net.novaware.nes.core.file.NesMeta.Kind;
import net.novaware.nes.core.file.NesMeta.Layout;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;

import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC;
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC_0_7;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Chars.isPrintable;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_512B;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.sint;

/**
 * Archaic iNES compatible header buffer
 * <p>
 * <a href="https://www.nesdev.org/wiki/INES">iNES on nesdev.org</a><br>
 * <a href="https://fms.komkon.org/EMUL8/NES.html#LABM">.NES File Format on fms.komkon.org</a>
 */
public class ArchaicHeaderBuffer extends BaseHeaderBuffer {

    // region Bytes 0-3

    public static final int BYTE_0 = 0;
    public static final MagicNumber MAGIC_NUMBER = MagicNumber.GAME_NES;

    // endregion
    // region Bytes 4-5

    public static final int BYTE_4 = 4;
    public static final @Unsigned byte PROGRAM_DATA_SIZE = ubyte(0xFF);

    public static final int BYTE_5 = 5;
    public static final @Unsigned byte VIDEO_DATA_SIZE   = ubyte(0xFF);

    // endregion
    // region Byte 6

    public static final int BYTE_6 = 6;
    public static final @Unsigned byte MAPPER_LO_BITS = ubyte(0b1111_0000);
    public static final @Unsigned byte LAYOUT_BITS    = ubyte(0b0000_1001);
    public static final @Unsigned byte TRAINER_BIT    = ubyte(0b0000_0100);
    public static final @Unsigned byte BATTERY_BIT    = ubyte(0b0000_0010);

    // endregion
    // region Byte 7 (iNES 0.7)

    public static final int  BYTE_7 = 7;
    public static final @Unsigned byte MAPPER_HI_BITS       = ubyte(0b1111_0000);
    public static final @Unsigned byte BYTE_7_RESERVED_BITS = ubyte(0b0000_1111);

    // endregion

    public ArchaicHeaderBuffer(UByteBuffer header) {
        super(header);
    }

    public ArchaicHeaderBuffer putMagic() {
        header.put(BYTE_0, MAGIC_NUMBER.numbers());
        return this;
    }

    public static @Unsigned byte[] getMagic(UByteBuffer header) {
        @Unsigned byte[] fourBytes = new byte[4];
        header.get(BYTE_0, fourBytes);

        return fourBytes;
    }

    public @Unsigned byte[] getMagic() {
        return getMagic(header);
    }

    public ArchaicHeaderBuffer putProgramData(Quantity programData) {
        assertArgument(programData.unit() == BANK_16KB, "program data size not in 16KB units");
        assertArgument(programData.amount() <= sint(PROGRAM_DATA_SIZE), "program data size exceeded");

        header.putAsByte(BYTE_4, programData.amount());

        return this;
    }

    public Quantity getProgramData() {
        int byte4 = header.getAsInt(BYTE_4);

        return new Quantity(byte4, BANK_16KB);
    }

    public ArchaicHeaderBuffer putVideoData(Quantity videoData) {
        assertArgument(videoData.unit() == BANK_8KB, "video data size not in 8KB units");
        assertArgument(videoData.amount() <= sint(VIDEO_DATA_SIZE), "video data size exceeded");

        header.putAsByte(BYTE_5, videoData.amount());

        return this;
    }

    public Quantity getVideoData() {
        int byte5 = header.getAsInt(BYTE_5);

        return new Quantity(byte5, BANK_8KB);
    }

    public IntPredicate getMapperRange(NesFileVersion version) {
        assertArchaicVersion(version);

        return switch(version) {
            case ARCHAIC -> mapper -> 0 <= mapper && mapper <= 0xF;
            case ARCHAIC_0_7 -> mapper -> 0 <= mapper && mapper <= 0xFF;
            default -> throw archaicAssertionError();
        };
    }

    /* package */ static UByteBuffer putMapperLo(UByteBuffer header, int mapper) {
        int byte6 = header.getAsInt(BYTE_6);

        int cleared = byte6 & ~sint(MAPPER_LO_BITS);
        int shifted = (mapper << 4) & sint(MAPPER_LO_BITS);

        return header.putAsByte(BYTE_6, cleared | shifted);
    }

    /* package */ static UByteBuffer putMapperHi(UByteBuffer header, int mapper) {
        assertArgument((mapper & ~sint(MAPPER_HI_BITS)) == 0,
                "mapper hi bits must be in their target position");

        int byte7 = header.getAsInt(BYTE_7);
        int cleared = byte7 & ~sint(MAPPER_HI_BITS);
        int bits = mapper & sint(MAPPER_HI_BITS);

        return header.putAsByte(BYTE_7, cleared | bits);
    }

    public ArchaicHeaderBuffer putMapper(NesFileVersion version, int mapper) {
        assertArchaicVersion(version);

        switch(version) {
            case ARCHAIC:
                assertArgument(getMapperRange(version).test(mapper), "Archaic mapper must be 0-15");
                putMapperLo(header, mapper);
                break;
            case ARCHAIC_0_7:
                assertArgument(getMapperRange(version).test(mapper), "iNES 0.7 mapper must be 0-255");

                putMapperLo(header, mapper & 0x0F);
                putMapperHi(header, mapper & 0xF0);
                break;

            default: throw archaicAssertionError();
        }

        return this;
    }

    /* package */ static int getMapperLo(UByteBuffer header) {
        int byte6 = header.getAsInt(BYTE_6);

        return (byte6 & sint(MAPPER_LO_BITS)) >> 4;
    }

    /* package */ static int getMapperHi(UByteBuffer header) {
        int byte7 = header.getAsInt(BYTE_7);

        return (byte7 & sint(MAPPER_HI_BITS));
    }

    public int getMapper(NesFileVersion version) {
        assertArchaicVersion(version);

        return switch(version) {
            case ARCHAIC -> getMapperLo(header);
            case ARCHAIC_0_7 -> getMapperHi(header) | getMapperLo(header);
            default -> throw archaicAssertionError();
        };
    }

    private AssertionError archaicAssertionError() throws AssertionError {
        return new AssertionError("unreachable, check method version assertion");
    }

    private void assertArchaicVersion(NesFileVersion version) {
        assertArgument(List.of(ARCHAIC, ARCHAIC_0_7).contains(version), "version must be one of archaic ones");
    }

    public int getByte7Reserved() { // TODO: report if not 0
        int byte7 = header.getAsInt(BYTE_7);

        return (byte7 & sint(BYTE_7_RESERVED_BITS));
    }

    public ArchaicHeaderBuffer putVideoMemoryLayout(Layout layout) {
        int byte6 = header.getAsInt(BYTE_6);
        int cleared = byte6 & ~sint(LAYOUT_BITS);
        int layoutBits = layout.bits();

        header.putAsByte(BYTE_6, cleared | layoutBits);

        return this;
    }

    public Layout getVideoMemoryLayout() {
        int byte6 = header.getAsInt(BYTE_6);

        int layoutBits = (byte6 & sint(LAYOUT_BITS));

        return Layout.fromBits(layoutBits);
    }

    public ArchaicHeaderBuffer putTrainer(Quantity trainer) {
        assertArgument(trainer.unit() == BANK_512B, "trainer size not in 512B units");
        assertArgument(trainer.amount() <= 1, "trainer size exceeded");

        int byte6 = header.getAsInt(BYTE_6);
        int cleared = byte6 & ~sint(TRAINER_BIT);
        int trainerBit = trainer.amount() == 1 ? sint(TRAINER_BIT) : 0;

        header.putAsByte(BYTE_6, cleared | trainerBit);

        return this;
    }

    public Quantity getTrainer() {
        int byte6 = header.getAsInt(BYTE_6);

        int trainerBit = (byte6 & sint(TRAINER_BIT)) >> 2;

        return new Quantity(trainerBit, BANK_512B);
    }

    public ArchaicHeaderBuffer putProgramMemoryKind(Kind kind) {
        int byte6 = header.getAsInt(BYTE_6);
        int cleared = byte6 & ~sint(BATTERY_BIT);
        int batteryBit = kind == Kind.PERSISTENT ? sint(BATTERY_BIT) : 0;

        header.putAsByte(BYTE_6, cleared | batteryBit);

        return this;
    }

    public Kind getProgramMemoryKind() {
        int byte6 = header.getAsInt(BYTE_6);

        int batteryBit = (byte6 & sint(BATTERY_BIT)) >> 1;

        return batteryBit == 0 ? Kind.VOLATILE : Kind.PERSISTENT;
    }

    // region Byte 7-15 for archaic, 8-15 for 0.7 TODO: improve the info methods and test better

    public ArchaicHeaderBuffer putInfo(String info) {
        assertArgument(info.length() < 10, "info too long to fit in the header");

        final @Unsigned byte[] infoBytes = info.getBytes(StandardCharsets.US_ASCII);

        for (int i = infoBytes.length - 1, j = header.capacity() - 1; i >= 0; i--, j--) {
            header.put(j, infoBytes[i]);
        }

        return this;
    }

    public String getInfo() {
        @Unsigned byte[] infoBytes = new byte[NesHeader.SIZE - 7];
        Arrays.fill(infoBytes, 0, infoBytes.length, (byte)' ');

        for(int i = infoBytes.length - 1; i >= 0; i--) {
            final int b = header.getAsInt(i + 7);

            if (isPrintable(b)) {
                infoBytes[i] = ubyte(b);
            } else {
                break; // read back until something strange
            }
        }

        return new String(infoBytes, StandardCharsets.US_ASCII).trim();
    }

    // endregion
}
