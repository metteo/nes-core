package net.novaware.nes.core.file;

import net.novaware.nes.core.file.NesFile.Kind;
import net.novaware.nes.core.file.NesFile.Mirroring;
import net.novaware.nes.core.file.NesFile.ProgramMemory;
import net.novaware.nes.core.file.NesFile.VideoStandard;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.Quantity.Unit;
import org.jspecify.annotations.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.novaware.nes.core.file.NesFile.Mirroring.FOUR_SCREEN;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Quantity.ZERO_BYTES;

// TODO: support Archaic iNES and NES 2.0 modes
// NOTE: NES 2.0 XML Database: https://forums.nesdev.org/viewtopic.php?t=19940

/*
 * Recommended detection procedure:
 *
 *     If byte 7 AND $0C = $08, and the size taking into account byte 9 does not exceed the actual size of the ROM image, then NES 2.0.
 *     If byte 7 AND $0C = $04, archaic iNES.
 *     If byte 7 AND $0C = $00, and bytes 12-15 are all 0, then iNES.
 *     Otherwise, iNES 0.7 or archaic iNES.
 */

public class NesFileReader {

    public enum Mode {
        /**
         * Throws an exception for any deviation from the iNES / NES 2.0 specification.
         */
        STRICT,

        /** Attempts to parse the file, logging warnings for minor issues
         * (e.g., truncated CHR) and only throwing exceptions for major,
         * blocking errors (e.g., corrupt header).
         */
        LENIENT
    }

    public record Result(NesFile nesFile, List<Problem> problems) {
    }

    public record Problem(Severity severity, String message) {
    }

    public enum Severity {
        MINOR,
        MAJOR
    }

    /**
     * "NES\x1a"
     * `\u001a` is a SUB character, Ctrl+Z, MS-DOS eof
     */
    /* package */ static final byte[] MAGIC_BYTES = new byte[]{ 0x4E, 0x45, 0x53, 0x1A };

    public static final int HEADER_SIZE = 16;

    public static final int PROGRAM_DATA_MULTIPLIER = 16 * 1024;
    public static final int PROGRAM_MEMORY_MULTIPLIER = 8 * 1024;
    public static final int VIDEO_DATA_MULTIPLIER = 8 * 1024;
    public static final int TRAINER_DATA_SIZE = 512;

    // TODO: add method for parsing header and returning just meta

    public Result read(Path path, Mode mode) throws NesFileReadingException { // TODO: test

        try (
            InputStream inputStream = Files.newInputStream(path);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)
        ) {
            return read(path.toAbsolutePath().toString(), bufferedInputStream, mode);
        } catch (IOException e) {
            throw new NesFileReadingException("Failed to read NES file: " + path, e);
        }
    }
    
    /**
     * Reads the input stream and deconstructs it according to header info
     * @param origin file path or url pointing to the file
     * @param inputStream caller is responsible for closing the stream
     * @param mode strictness of the reader
     */
    public Result read(String origin, InputStream inputStream, Mode mode) throws NesFileReadingException {
        assertArgument(origin != null && !origin.isEmpty(), "origin must be provided");
        assertArgument(inputStream != null, "inputStream must be provided");
        assertArgument(mode != null, "mode must be provided");

        var inputBuffer = readInputStream(origin, inputStream);

        var headerBuffer = inputBuffer.slice(0, HEADER_SIZE);

        validateMagicBytes(headerBuffer);

        int programDataSize = Byte.toUnsignedInt(headerBuffer.get()) * PROGRAM_DATA_MULTIPLIER;
        int videoDataSize = Byte.toUnsignedInt(headerBuffer.get()) * VIDEO_DATA_MULTIPLIER;

        // NOTE: assumption for iNES, read properly in NES 2.0
        Quantity videoMemory = (videoDataSize == 0) ? new Quantity(8 * 1024, Unit.BYTES) : ZERO_BYTES;

        byte flags6 = headerBuffer.get();
        byte flags7 = headerBuffer.get();
        byte flags8 = headerBuffer.get();
        byte flags9 = headerBuffer.get();
        byte flags10 = headerBuffer.get();

        byte[] padding = new byte[5]; // TODO: verify 0s for iNES
        headerBuffer.get(padding);

        // region Flags 6

        Mirroring mirroring = isBitSet(flags6, 0) ? Mirroring.HORIZONTAL : Mirroring.VERTICAL;

        boolean persistentProgramMemory = isBitSet(flags6, 1);

        boolean trainerPresent = isBitSet(flags6, 2);
        int trainerSize = trainerPresent ? TRAINER_DATA_SIZE : 0;

        boolean alternativeMirroring = isBitSet(flags6, 3);
        if (alternativeMirroring) {
            mirroring = FOUR_SCREEN; // TODO: or single screen?
        }

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

        VideoStandard videoStandard = isBitSet(flags9, 0) ? VideoStandard.PAL : VideoStandard.NTSC;

        int flag9zeroes = (flags9 & 0xFE) >> 1;
        if (flag9zeroes > 0) {
            throw new NesFileReadingException("flag9 reserved area not 0s: " + flag9zeroes); // TODO: don't throw, see note above
        }

        // endregion
        // region Flags 10

        int videoStandardBits = flags10 & 0b11;
        VideoStandard videoStandard2 = switch (videoStandardBits) {
            case 0 -> VideoStandard.NTSC;
            case 1 -> VideoStandard.NTSC_HYBRID;
            case 2 -> VideoStandard.PAL;
            case 3 -> VideoStandard.PAL_HYBRID; // TODO: Dendy?
            default -> VideoStandard.OTHER; // TODO: report a problem
        };

        boolean programMemoryPresent = !isBitSet(flags10, 4); // TODO: may conflict with the size byte, resolve

        ProgramMemory programMemory = programMemoryPresent
                ? new ProgramMemory(
                    persistentProgramMemory ? Kind.PERSISTENT : Kind.VOLATILE,
                    new Quantity(programMemorySize, Unit.BYTES)
                )
                : new ProgramMemory(Kind.NONE, ZERO_BYTES);

        boolean busConflicts = isBitSet(flags10, 5);

        // endregion

        NesFile.Meta meta = NesFile.Meta.builder()
                .title(origin.substring(origin.lastIndexOf('/') + 1))
                .info("") // TODO: read end of the header
                .system(NesFile.System.NES)
                .mapper(mapper)
                .busConflicts(busConflicts)
                .trainer(new Quantity(trainerPresent ? 1 : 0, Unit.BANK_512B)) // TODO: figure out a better way for units
                .programMemory(programMemory)
                .programData(new Quantity(programDataSize, Unit.BYTES))
                .videoMemory(videoMemory)
                .videoData(new Quantity(videoDataSize, Unit.BYTES))
                .videoStandard(videoStandard2) // TODO: resolve conflicts with videoStandard variable
                .mirroring(mirroring)
                .remainder(new Quantity(0, Unit.BYTES))
                .build();


        // FIXME: check if there is enough data before slicing. there are truncated roms out there.

        var trainerData = trainerPresent
                ? inputBuffer.slice(HEADER_SIZE, trainerSize)
                : ByteBuffer.allocate(0);

        var programData = inputBuffer.slice(HEADER_SIZE + trainerSize, programDataSize);
        var videoData = videoDataSize > 0
                ? inputBuffer.slice(HEADER_SIZE + trainerSize + programDataSize, videoDataSize)
                : ByteBuffer.allocate(0);

        int expectedDataAmount = HEADER_SIZE + trainerSize + programDataSize + videoDataSize;
        int inputBufferSize = inputBuffer.capacity();

        if (inputBufferSize < expectedDataAmount) {
            throw new IllegalArgumentException("File is truncated. Expected " + expectedDataAmount + " bytes but got " + inputBufferSize);
        }

        var remainingData = inputBuffer.slice(expectedDataAmount, inputBufferSize - expectedDataAmount); // TODO: verify

        NesFile.Data data = new NesFile.Data(
                headerBuffer.rewind(),
                trainerData,
                programData,
                videoData,
                ByteBuffer.allocate(0),
                ByteBuffer.allocate(0),
                remainingData
        );

        NesFile nesFile = new NesFile(origin, meta, data);
        return new Result(nesFile, List.of());
    }

    private static @NonNull ByteBuffer readInputStream(String origin, InputStream inputStream) {
        final byte[] inputBytes;
        try {
            inputBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new NesFileReadingException("Unable to read input bytes of: " + origin + " ", e);
        }
        var inputBuffer = ByteBuffer.wrap(inputBytes);
        inputBuffer.order(LITTLE_ENDIAN);

        if (inputBuffer.capacity() < HEADER_SIZE) {
            throw new NesFileReadingException("Input data is too short to contain iNES header");
        }
        return inputBuffer;
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
