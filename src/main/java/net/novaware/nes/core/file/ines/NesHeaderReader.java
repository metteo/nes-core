package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Quantity;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static net.novaware.nes.core.file.NesMeta.Layout.ALTERNATIVE_HORIZONTAL;
import static net.novaware.nes.core.file.NesMeta.Layout.ALTERNATIVE_VERTICAL;
import static net.novaware.nes.core.util.Quantity.ZERO_BYTES;

public class NesHeaderReader extends NesHeaderHandler {

    /**
     * "NES\x1a"
     * `\u001a` is a SUB character, Ctrl+Z, MS-DOS eof
     */
    /* package */ static final byte[] MAGIC_BYTES = new byte[]{ 0x4E, 0x45, 0x53, 0x1A };

    public static final int PROGRAM_DATA_MULTIPLIER = 16 * 1024;
    public static final int PROGRAM_MEMORY_MULTIPLIER = 8 * 1024;
    public static final int VIDEO_DATA_MULTIPLIER = 8 * 1024;
    public static final int TRAINER_DATA_SIZE = 512;

    public record Result(NesMeta meta, List<NesFileReader.Problem> problems) {
    }

    public Result read(URI origin, ByteBuffer headerBuffer, NesFileReader.Mode mode) {
        validateMagicBytes(headerBuffer);

        int programDataSize = Byte.toUnsignedInt(headerBuffer.get()) * PROGRAM_DATA_MULTIPLIER;
        int videoDataSize = Byte.toUnsignedInt(headerBuffer.get()) * VIDEO_DATA_MULTIPLIER;

        // NOTE: assumption for iNES, read properly in NES 2.0
        Quantity videoMemory = (videoDataSize == 0) ? new Quantity(8 * 1024, Quantity.Unit.BYTES) : ZERO_BYTES;

        byte flags6 = headerBuffer.get();
        byte flags7 = headerBuffer.get();
        byte flags8 = headerBuffer.get();
        byte flags9 = headerBuffer.get();
        byte flags10 = headerBuffer.get();

        byte[] padding = new byte[5]; // TODO: verify 0s for iNES
        headerBuffer.get(padding);

        // region Flags 6

        boolean horizontalLayout = isBitSet(flags6, 0);
        boolean alternativeLayout = isBitSet(flags6, 3);

        NesMeta.Layout layout = alternativeLayout ?
                (horizontalLayout ? ALTERNATIVE_HORIZONTAL : ALTERNATIVE_VERTICAL) :
                (horizontalLayout ? NesMeta.Layout.STANDARD_HORIZONTAL : NesMeta.Layout.STANDARD_VERTICAL);

        boolean persistentProgramMemory = isBitSet(flags6, 1);

        boolean trainerPresent = isBitSet(flags6, 2);
        int trainerSize = trainerPresent ? TRAINER_DATA_SIZE : 0;

        int mapperLo = (flags6 & 0xF0) >> 4;

        // endregion
        // region Flags 7

        boolean vsUnisystem = isBitSet(flags7, 0); // Vs. games have a coin slot and different palettes.
        boolean playChoice10 = isBitSet(flags7, 1); // 8 KB of Hint Screen data stored after CHR data

        // TODO: detect version first and delegate to specialized reading method.
        int nesVersionBits = ((flags7 & 0b1100) >> 2);
        boolean archaicNes = nesVersionBits == 1;
        boolean ines = nesVersionBits == 0; // TODO: AND bytes 12-15 are all 0
        boolean nes2 = nesVersionBits == 2;
        boolean ines_0_7_or_archaic = !(archaicNes | ines | nes2);

        if (vsUnisystem || playChoice10 || nes2) {
            throw new NesFileReadingException("vsUnisystem / playChoice10 / nes2 not supported");
            // FIXME: report a warning
        }

        int mapperHi = (flags7 & 0xF0);
        short mapper = (short) (mapperHi | mapperLo); // TODO: DiskDude! may increase the mapper number by 64!

        // endregion
        // region Flags 8

        int programMemoryByte = Byte.toUnsignedInt(flags8);
        int programMemorySize = programMemoryByte == 0 // Size of PRG RAM in 8 KB units
                ? PROGRAM_MEMORY_MULTIPLIER // Value 0 infers 8 KB for compatibility
                : programMemoryByte * PROGRAM_MEMORY_MULTIPLIER;// TODO: uint() from chip8

        // endregion
        // region Flags 9

        NesMeta.VideoStandard videoStandard = isBitSet(flags9, 0) ? NesMeta.VideoStandard.PAL : NesMeta.VideoStandard.NTSC;

        int flag9zeroes = (flags9 & 0xFE) >> 1;
        if (flag9zeroes > 0) {
            throw new NesFileReadingException("flag9 reserved area not 0s: " + flag9zeroes); // TODO: don't throw, see note above
        }

        // endregion
        // region Flags 10

        int videoStandardBits = flags10 & 0b11;
        NesMeta.VideoStandard videoStandard2 = switch (videoStandardBits) {
            case 0 -> NesMeta.VideoStandard.NTSC;
            case 1 -> NesMeta.VideoStandard.NTSC_HYBRID;
            case 2 -> NesMeta.VideoStandard.PAL;
            case 3 -> NesMeta.VideoStandard.PAL_HYBRID; // TODO: Dendy?
            default -> NesMeta.VideoStandard.OTHER; // TODO: report a problem
        };

        boolean programMemoryPresent = !isBitSet(flags10, 4); // TODO: may conflict with the size byte, resolve

        NesMeta.ProgramMemory programMemory = programMemoryPresent
                ? new NesMeta.ProgramMemory(
                persistentProgramMemory ? NesMeta.Kind.PERSISTENT : NesMeta.Kind.VOLATILE,
                new Quantity(programMemorySize, Quantity.Unit.BYTES)
        )
                : new NesMeta.ProgramMemory(NesMeta.Kind.NONE, ZERO_BYTES);

        boolean busConflicts = isBitSet(flags10, 5);

        // endregion

        NesMeta meta = NesMeta.builder()
                .title(Paths.get(origin).getFileName().toString()) // TODO: remove extension?
                .info("") // TODO: read end of the header
                .system(NesMeta.System.NES)
                .mapper(mapper)
                .busConflicts(busConflicts)
                .trainer(new Quantity(trainerPresent ? 1 : 0, Quantity.Unit.BANK_512B)) // TODO: figure out a better way for units
                .programMemory(programMemory)
                .programData(new Quantity(programDataSize, Quantity.Unit.BYTES))
                .videoMemory(videoMemory)
                .videoData(new NesMeta.VideoData(layout, new Quantity(videoDataSize, Quantity.Unit.BYTES)))
                .videoStandard(videoStandard2) // TODO: resolve conflicts with videoStandard variable
                .footer(new Quantity(0, Quantity.Unit.BYTES))
                .build();

        return new Result(meta, List.of());
    }

    private static void validateMagicBytes(ByteBuffer headerBuffer) {
        byte[] fourBytes = new byte[4];
        headerBuffer.get(fourBytes);

        if (!Arrays.equals(fourBytes, MAGIC_BYTES)) {
            throw new NesFileReadingException("Invalid magic bytes: " + Hex.s(fourBytes)); // TODO: don't throw, see note above
        }
    }

    private static boolean isBitSet(byte b, int bitIndex) {
        return (b & (1 << bitIndex)) != 0;
    }
}
