package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.NesMeta.VideoStandard;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.function.IntPredicate;

import static net.novaware.nes.core.file.NesMeta.VideoStandard.DENDY;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC_DUAL;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.PAL;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.PAL_DUAL;
import static net.novaware.nes.core.file.ines.ArchaicHeaderBuffer.getMapperLo;
import static net.novaware.nes.core.file.ines.ArchaicHeaderBuffer.putMapperLo;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

/**
 * The iNES 1.0 compatible header buffer
 * <p>
 * Contains methods for iNES 0.7 (mapper hi bits) and both
 * official and unofficial extensions
 * <p>
 * <a href="https://www.nesdev.org/wiki/INES">iNES on nesdev.org</a><br>
 * <a href="https://www.nesdev.org/iNES.txt">iNES format by rvu (2000)</a><br>
 * <a href="https://www.nesdev.org/neshdr20.txt">iNES Header/Format by VmprHntrD (1998)</a>
 */
public class ModernHeaderBuffer extends BaseHeaderBuffer {

    // region Byte 7

    public static final int  BYTE_7 = 7;
    public static final @Unsigned byte MAPPER_HI_BITS   = ubyte(0b1111_0000); // "iNES 0.7"
    public static final @Unsigned byte VERSION_BITS     = ubyte(0b0000_1100);
    public static final @Unsigned byte SYSTEM_TYPE_BITS = ubyte(0b0000_0011);

    // endregion
    // region Byte 8 (rare)

    public static final int BYTE_8 = 8;
    public static final @Unsigned byte PROGRAM_MEMORY_SIZE = ubyte(0xFF);

    // endregion
    // region Byte 9 (rare)

    public static final int BYTE_9 = 9;
    public static final @Unsigned byte BYTE_9_RESERVED_BITS = ubyte(0b1111_1110);
    public static final @Unsigned byte VIDEO_STANDARD_BITS  = ubyte(0b0000_0001);

    // endregion
    // region Byte 10 (unofficial)

    public static final int BYTE_10 = 10;
    public static final @Unsigned byte BYTE_10_RESERVED_BITS      = ubyte(0b1100_1100);
    public static final @Unsigned byte BUS_CONFLICTS_BIT          = ubyte(0b0010_0000);
    public static final @Unsigned byte PROGRAM_MEMORY_ABSENT_BIT  = ubyte(0b0001_0000);
    public static final @Unsigned byte VIDEO_STANDARD_EXT_BITS    = ubyte(0b0000_0011);

    // endregion

    public ModernHeaderBuffer(UByteBuffer header) {
        super(header);
    }

    public IntPredicate getMapperRange() {
        return mapper -> 0 <= mapper && mapper <= 0xFF;
    }

    public ModernHeaderBuffer putMapper(int mapper) {
        assertArgument(getMapperRange().test(mapper), "Modern mapper must be 0-255");

        int mapperLo = mapper & 0x0F;
        int mapperHi = mapper & 0xF0;

        putMapperLo(header, mapperLo);
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
        int mapperHi = getMapperHi();
        int mapperLo = getMapperLo(header);

        return mapperHi | mapperLo;
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

    public ModernHeaderBuffer putProgramMemory(Quantity programMemory) {
        assertArgument(programMemory.unit() == BANK_8KB, "program memory size not in 8KB units");
        assertArgument(programMemory.amount() <= uint(PROGRAM_MEMORY_SIZE), "program memory size exceeded");

        header.putAsByte(BYTE_8, programMemory.amount());

        return this;
    }

    public Quantity getProgramMemory() {
        int byte8 = header.getAsInt(BYTE_8);

        return new Quantity(byte8, BANK_8KB);
    }

    public ModernHeaderBuffer putVideoStandard(VideoStandard videoStandard) {
        assertArgument(videoStandard != null, "video standard cannot be null");
        // TODO: validate the input for allowed values: NTSC, PAL, UNKNOWN

        int byte9 = header.getAsInt(BYTE_9);
        int cleared = byte9 & ~uint(VIDEO_STANDARD_BITS);

        int bit = switch(videoStandard) {
            default              -> 0b0;
            case NTSC, NTSC_DUAL -> 0b0;
            case PAL, PAL_DUAL   -> 0b1;
        };

        header.putAsByte(BYTE_9, cleared | bit);

        return this;
    }

    public VideoStandard getVideoStandard() {
        int byte9 = header.getAsInt(BYTE_9);
        int bit = (byte9 & uint(VIDEO_STANDARD_BITS));

        return bit == 1 ? PAL : NTSC;
    }

    public int getByte9Reserved() { // TODO: report if not 0
        int byte9 = header.getAsInt(BYTE_9);

        return (byte9 & uint(BYTE_9_RESERVED_BITS)) >> 1;
    }

    // region Byte 10 Methods (unofficial)

    public ModernHeaderBuffer putBusConflicts(boolean busConflicts) {
        int byte10 = header.getAsInt(BYTE_10);

        int cleared = byte10 & ~uint(BUS_CONFLICTS_BIT);
        int busConflictsBit = busConflicts ? uint(BUS_CONFLICTS_BIT) : 0;

        header.putAsByte(BYTE_10, cleared | busConflictsBit);

        return this;
    }

    public boolean getBusConflicts() {
        int byte10 = header.getAsInt(BYTE_10);

        int busConflictsBit = byte10 & uint(BUS_CONFLICTS_BIT);

        return busConflictsBit != 0;
    }

    public ModernHeaderBuffer putProgramMemoryAbsent(boolean absent) {
        int byte10 = header.getAsInt(BYTE_10);

        int cleared = byte10 & ~uint(PROGRAM_MEMORY_ABSENT_BIT);
        int absenceBit = absent ? uint(PROGRAM_MEMORY_ABSENT_BIT) : 0;

        header.putAsByte(BYTE_10, cleared | absenceBit);

        return this;
    }

    public boolean isProgramMemoryAbsent() {
        int byte10 = header.getAsInt(BYTE_10);

        int bit = byte10 & uint(PROGRAM_MEMORY_ABSENT_BIT);

        return bit != 0;
    }

    public ModernHeaderBuffer putVideoStandardExt(VideoStandard videoStandard) {
        assertArgument(videoStandard != null, "video standard cannot be null");
        assertArgument(videoStandard != DENDY, "video standard cannot be Dendy");

        int byte10 = header.getAsInt(BYTE_10);
        int cleared = byte10 & ~uint(VIDEO_STANDARD_EXT_BITS);

        int bit = switch(videoStandard) { // TODO: consider moving to the enum
            default        -> 0b00;
            case NTSC      -> 0b00;
            case NTSC_DUAL -> 0b01;
            case PAL       -> 0b10;
            case PAL_DUAL  -> 0b11;
        };

        header.putAsByte(BYTE_10, cleared | bit);

        return this;
    }

    public VideoStandard getVideoStandardExt() {
        int byte10 = header.getAsInt(BYTE_10);
        int bits = (byte10 & uint(VIDEO_STANDARD_EXT_BITS));

        return switch (bits) {
            default   -> NTSC;
            case 0b00 -> NTSC;
            case 0b01 -> NTSC_DUAL;
            case 0b10 -> PAL;
            case 0b11 -> PAL_DUAL;
        };
    }

    public int getByte10Reserved() { // TODO: report if not 0
        int byte10 = header.getAsInt(BYTE_10);

        return byte10 & uint(BYTE_10_RESERVED_BITS);
    }

    // endregion
}
