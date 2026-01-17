package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class ModernHeaderBuffer {

    // region Byte 7

    public static final int  BYTE_7 = 7;
    public static final @Unsigned byte MAPPER_HI_BITS   = ubyte(0b1111_0000);
    public static final @Unsigned byte VERSION_BITS     = ubyte(0b0000_1100);
    public static final @Unsigned byte SYSTEM_TYPE_BITS = ubyte(0b0000_0011);

    // endregion

    private final UByteBuffer header;

    /**
     * Reuse some parsing, composition over inheritance FTW
     */
    private final ArchaicHeaderBuffer archaicHeader;

    public ModernHeaderBuffer(UByteBuffer header) {
        assertArgument(header != null, "header cannot be null");
        assertArgument(header.capacity() == NesHeader.SIZE, "header must be " + NesHeader.SIZE + " bytes");

        this.header = header;
        this.archaicHeader = new ArchaicHeaderBuffer(header);
    }

    public ModernHeaderBuffer putMagic() {
        archaicHeader.putMagic();
        return this;
    }

    public @Unsigned byte[] getMagic() {
        return archaicHeader.getMagic();
    }

    public ModernHeaderBuffer putProgramData(Quantity programData) {
        archaicHeader.putProgramData(programData);
        return this;
    }

    public Quantity getProgramData() {
        return archaicHeader.getProgramData();
    }

    public ModernHeaderBuffer putVideoData(Quantity videoData) {
        archaicHeader.putVideoData(videoData);
        return this;
    }

    public Quantity getVideoData() {
        return archaicHeader.getVideoData();
    }

    public ModernHeaderBuffer putMapper(int mapper) {
        assertArgument(mapper >= 0 && mapper <= 255, "Archaic mapper must be 0-255");

        int mapperLo = mapper & 0x0F;
        int mapperHi = mapper & 0xF0;

        archaicHeader.putMapper(mapperLo);
        this.putMapperHi(mapperHi);

        return this;
    }

    private void putMapperHi(int mapper) {
        assertArgument((mapper & ~uint(MAPPER_HI_BITS)) == 0,
                "mapper hi bits must be in their target position");

        int byte7 = header.getAsInt(BYTE_7);
        int cleared = byte7 & ~uint(MAPPER_HI_BITS);
        int bits = mapper & uint(MAPPER_HI_BITS);

        header.putAsByte(BYTE_7, cleared | bits);
    }

    public int getMapper() {
        int mapperLo = archaicHeader.getMapper();
        int mapperHi = getMapperHi();

        return mapperLo | mapperHi;
    }

    private int getMapperHi() {
        int byte7 = header.getAsInt(BYTE_7);

        return (byte7 & uint(MAPPER_HI_BITS));
    }

    public ModernHeaderBuffer putSystem(NesMeta.System system) {
        assertArgument(system != null, "system cannot be null");

        int byte7 = header.getAsInt(BYTE_7);
        int cleared = byte7 & ~uint(SYSTEM_TYPE_BITS);
        int bits = system.bits();

        header.putAsByte(BYTE_7, cleared | bits);

        return this;
    }

    public NesMeta.System getSystem() {
        int byte7 = header.getAsInt(BYTE_7);

        int systemBits = (byte7 & uint(SYSTEM_TYPE_BITS));

        return NesMeta.System.fromBits(systemBits);
    }

    public ModernHeaderBuffer putVersion(int version) {
        assertArgument(0b00 <= version && version <= 0b11, "version must be 0b00-0b11");

        int byte7 = header.getAsInt(BYTE_7);
        int cleared = byte7 & ~uint(VERSION_BITS);
        int shifted = (version << 2) & uint(VERSION_BITS);

        header.putAsByte(BYTE_7, cleared | shifted);

        return this;
    }

    public static int getVersion(UByteBuffer header) {
        int byte7 = header.getAsInt(BYTE_7);

        return (byte7 & uint(VERSION_BITS)) >> 2;
    }

    public int getVersion() {
        return getVersion(header);
    }
}
