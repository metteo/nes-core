package net.novaware.nes.core.file;

import net.novaware.nes.core.file.NesFile.Kind;
import net.novaware.nes.core.file.NesFile.Mirroring;
import net.novaware.nes.core.file.NesFile.ProgramMemory;
import net.novaware.nes.core.file.NesFile.VideoStandard;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.Quantity.Unit;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static net.novaware.nes.core.util.Quantity.ZERO_BYTES;

// TODO: support Archaic / iNES and NES2.0 modes

/*
 * Recommended detection procedure:
 *
 *     If byte 7 AND $0C = $08, and the size taking into account byte 9 does not exceed the actual size of the ROM image, then NES 2.0.
 *     If byte 7 AND $0C = $04, archaic iNES.
 *     If byte 7 AND $0C = $00, and bytes 12-15 are all 0, then iNES.
 *     Otherwise, iNES 0.7 or archaic iNES.
 */

public class NesFileReader {

    /**
     * "NES\x1a"
     * `\u001a` is a SUB character, Ctrl+Z, MS-DOS eof
     */
    public static final String MAGIC_STRING = "NES\u001a";

    public static final int HEADER_SIZE = 16;

    public static final int PROGRAM_DATA_MULTIPLIER = 16 * 1024;
    public static final int PROGRAM_MEMORY_MULTIPLIER = 8 * 1024;
    public static final int VIDEO_DATA_MULTIPLIER = 8 * 1024;
    public static final int TRAINER_DATA_SIZE = 512;

    static {
        assert MAGIC_STRING.getBytes(StandardCharsets.UTF_8).length == 4; // TODO: make internal check it equals to BYTES
    }

    public NesFile read(Path path) { // TODO: test
        try (
            InputStream inputStream = Files.newInputStream(path);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)
        ) {
            return read(path.toAbsolutePath().toString(), bufferedInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: throw a business exception
        }
    }
    
    /**
     * Reads the input stream and deconstructs it according to header info
     * @param origin file path or url pointing to the file
     * @param inputStream caller is responsible for closing the stream
     */
    public NesFile read(String origin, InputStream inputStream) throws IOException {
        // TODO: input validation

        final byte[] inputBytes = inputStream.readAllBytes();
        var inputBuffer = ByteBuffer.wrap(inputBytes);

        var headerBuffer = inputBuffer.slice(0, HEADER_SIZE);

        validateMagicBytes(headerBuffer);

        int programDataSize = headerBuffer.get() * PROGRAM_DATA_MULTIPLIER;
        int videoDataSize = headerBuffer.get() * VIDEO_DATA_MULTIPLIER;

        byte flags6 = headerBuffer.get();
        byte flags7 = headerBuffer.get();
        byte flags8 = headerBuffer.get();
        byte flags9 = headerBuffer.get();
        byte flags10 = headerBuffer.get();

        byte[] padding = new byte[5];
        headerBuffer.get(padding);

        int mirroringBit = (flags6 & 0b1);
        Mirroring mirroring = mirroringBit == 0 ? Mirroring.VERTICAL : Mirroring.HORIZONTAL;

        // TODO: create utility for single bit reads
        boolean persistentProgramMemory = ((flags6 & 0b10) >> 1) == 1;

        boolean trainerPresent = ((flags6 & 1 << 2) >> 2) == 1;
        int trainerSize = trainerPresent ? TRAINER_DATA_SIZE : 0;

        boolean alternativeMirroring = ((flags6 & 1 << 3) >> 3) == 1;

        if (alternativeMirroring) {
            throw new RuntimeException("alternative mirroring not supported");
        }

        int mapperLo = (flags6 & 0xF0) >> 4;

        boolean vsUnisystem = (flags7 & 0b1) == 1; // Vs. games have a coin slot and different palettes.
        boolean playChoice10 = ((flags7 & 0b10) >> 1) == 1; // 8 KB of Hint Screen data stored after CHR data

        // TODO: detect version first and delegate to specialized reading method.
        int nesVersionBits = ((flags7 & 0b1100) >> 2);
        boolean archaicNes = nesVersionBits == 1;
        boolean ines = nesVersionBits == 0; // TODO: AND bytes 12-15 are all 0
        boolean nes2 = nesVersionBits == 2;
        boolean ines_0_7_or_archaic = !(archaicNes | ines | nes2);

        if (vsUnisystem || playChoice10 || nes2) {
            throw new RuntimeException("vsUnisystem / playChoice10 / nes2 not supported");
        }

        int mapperHi = (flags7 & 0xF0);
        short mapper = (short) (mapperHi | mapperLo);

        int programMemoryByte = Byte.toUnsignedInt(flags8);
        int programMemorySize = programMemoryByte == 0 // Size of PRG RAM in 8 KB units
                ? PROGRAM_MEMORY_MULTIPLIER // Value 0 infers 8 KB for compatibility
                : programMemoryByte * PROGRAM_MEMORY_MULTIPLIER;// TODO: uint() from chip8

        int videoStandardBit = flags9 & 0b1;
        VideoStandard videoStandard = videoStandardBit == 0 ? VideoStandard.NTSC : VideoStandard.PAL;

        int flag9zeroes = (flags9 & 0xFE) >> 1;
        if (flag9zeroes > 0) {
            throw new RuntimeException("flag9 reserved area not 0s: " + flag9zeroes);
        }

        int videoStandardBits = flags10 & 0b11;
        VideoStandard videoStandard2 = switch (videoStandardBits) {
            case 0 -> VideoStandard.NTSC;
            case 1 -> VideoStandard.NTSC_HYBRID;
            case 2 -> VideoStandard.PAL;
            case 3 -> VideoStandard.PAL_HYBRID;
            default -> VideoStandard.OTHER;
        };

        int programMemoryBit = (flags10 & 1 << 4) >> 4;
        boolean programMemoryPresent = programMemoryBit == 0;
        ProgramMemory programMemory = programMemoryPresent
                ? new ProgramMemory(Kind.NONE, ZERO_BYTES)
                : new ProgramMemory(
                    persistentProgramMemory ? Kind.PERSISTENT : Kind.VOLATILE,
                    new Quantity(programMemorySize, Unit.BYTES)
                );

        Quantity videoMemory = ZERO_BYTES; // TODO: figure out how to read it in iNES format

        int busConflictsBit = (flags10 & 1 << 5) >> 5;
        boolean busConflicts = busConflictsBit == 1;

        var trainerData = trainerPresent
                ? inputBuffer.slice(HEADER_SIZE, trainerSize)
                : ByteBuffer.allocate(0);

        var programData = inputBuffer.slice(HEADER_SIZE + trainerSize, programDataSize);
        var videoData = inputBuffer.slice(HEADER_SIZE + trainerSize + programDataSize, videoDataSize); // TODO: optional

        int concreteDataAmount = HEADER_SIZE + trainerSize + programDataSize + videoDataSize;
        int inputBufferSize = inputBuffer.capacity();

        var remainingData = inputBuffer.slice(concreteDataAmount, inputBufferSize - concreteDataAmount); // TODO: verify

        return new NesFile(
                origin,
                mapper,
                busConflicts,
                programMemory,
                programData,
                videoMemory,
                videoData,
                videoStandard2, // TODO: resolve conflicts with videoStandard variable
                mirroring,
                headerBuffer,
                trainerData,
                remainingData
        );
    }

    private static void validateMagicBytes(ByteBuffer headerBuffer) {
        byte[] fourBytes = new byte[4];
        headerBuffer.get(fourBytes);

        byte[] magicBytes = MAGIC_STRING.getBytes(StandardCharsets.UTF_8);

        if (!Arrays.equals(fourBytes, magicBytes)) {
            throw new IllegalArgumentException("Input bytes don't follow iNES format");
        }
    }
}
