package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.MagicNumber;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.charset.StandardCharsets;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_512B;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class ArchaicHeaderBuffer {

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

    private final UByteBuffer header;

    public ArchaicHeaderBuffer(UByteBuffer header) {
        assertArgument(header != null, "header cannot be null");
        assertArgument(header.capacity() == NesHeader.SIZE, "header must be " + NesHeader.SIZE + " bytes");

        this.header = header;
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
        assertArgument(programData.amount() <= uint(PROGRAM_DATA_SIZE), "program data size exceeded");

        header.putAsByte(BYTE_4, programData.amount());

        return this;
    }

    public Quantity getProgramData() {
        int byte4 = header.getAsInt(BYTE_4);

        return new Quantity(byte4, BANK_16KB);
    }

    public ArchaicHeaderBuffer putVideoData(Quantity videoData) {
        assertArgument(videoData.unit() == BANK_8KB, "video data size not in 8KB units");
        assertArgument(videoData.amount() <= uint(VIDEO_DATA_SIZE), "video data size exceeded");

        header.putAsByte(BYTE_5, videoData.amount());

        return this;
    }

    public Quantity getVideoData() {
        int byte5 = header.getAsInt(BYTE_5);

        return new Quantity(byte5, BANK_8KB);
    }

    public ArchaicHeaderBuffer putMapper(int mapper) {
        assertArgument(mapper >= 0 && mapper <= 15, "Archaic mapper must be 0-15");

        int byte6 = header.getAsInt(BYTE_6);
        int cleared = byte6 & ~uint(MAPPER_LO_BITS);
        int shifted = (mapper << 4) & uint(MAPPER_LO_BITS);

        header.putAsByte(BYTE_6, cleared | shifted);

        return this;
    }

    public int getMapper() {
        int byte6 = header.getAsInt(BYTE_6);

        return (byte6 & uint(MAPPER_LO_BITS)) >> 4;
    }

    public ArchaicHeaderBuffer putMemoryLayout(NesMeta.Layout layout) {
        int byte6 = header.getAsInt(BYTE_6);
        int cleared = byte6 & ~uint(LAYOUT_BITS);
        int bits = layout.bits();

        header.putAsByte(BYTE_6, cleared | bits);

        return this;
    }

    public NesMeta.Layout getMemoryLayout() {
        int byte6 = header.getAsInt(BYTE_6);

        int layoutBits = (byte6 & uint(LAYOUT_BITS));

        return NesMeta.Layout.fromBits(layoutBits);
    }

    public ArchaicHeaderBuffer putTrainer(Quantity trainer) {
        assertArgument(trainer.unit() == BANK_512B, "trainer size not in 512B units");
        assertArgument(trainer.amount() <= 1, "trainer size exceeded");

        int byte6 = header.getAsInt(BYTE_6);
        int cleared = byte6 & ~uint(TRAINER_BIT);
        int bit = trainer.amount() == 1 ? uint(TRAINER_BIT) : 0;

        header.putAsByte(BYTE_6, cleared | bit);

        return this;
    }

    public Quantity getTrainer() {
        int byte6 = header.getAsInt(BYTE_6);

        int trainerBit = (byte6 & uint(TRAINER_BIT)) >> 2;

        return new Quantity(trainerBit, BANK_512B);
    }

    public ArchaicHeaderBuffer putMemoryKind(NesMeta.Kind kind) {
        int byte6 = header.getAsInt(BYTE_6);
        int cleared = byte6 & ~uint(BATTERY_BIT);
        int bit = kind == NesMeta.Kind.PERSISTENT ? uint(BATTERY_BIT) : 0;

        header.putAsByte(BYTE_6, cleared | bit);

        return this;
    }

    public NesMeta.Kind getMemoryKind() {
        int byte6 = header.getAsInt(BYTE_6);

        int batteryBit = (byte6 & uint(BATTERY_BIT)) >> 1;

        return batteryBit == 0 ? NesMeta.Kind.VOLATILE : NesMeta.Kind.PERSISTENT;
    }

    // region Byte 7-15 only archaic TODO: improve the info methods and test better

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

        // NOTE: may arrive at some random printable character, doesn't mean it's an info text
        for(int i = 0; i < infoBytes.length; i++) {
            final int b = header.getAsInt(i + 7);

            if (32 <= b && b < 127) {
                infoBytes[i] = ubyte(b);
            } else {
                infoBytes[i] = ' '; // any nonprintable char into space
            }
        }

        return new String(infoBytes, StandardCharsets.US_ASCII).trim();
    }

    // endregion
}
