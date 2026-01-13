package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesData;
import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesHash;
import org.jspecify.annotations.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.novaware.nes.core.util.Asserts.assertArgument;

// TODO: support Archaic iNES and NES 2.0 modes
// NOTE: NES 2.0 XML Database: https://forums.nesdev.org/viewtopic.php?t=19940

/**
 * Steps:
 *  1. Scanning for magic numbers and version
 *  2. Reading the header for metadata
 *  3. Slicing the data sections
 *  4. Hashing the data sections
 */
public class NesFileReader extends NesFileHandler {

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

    // TODO: add method for parsing header and returning just meta

    public Result read(URI origin, Mode mode) throws NesFileReadingException { // TODO: test
        assertArgument(origin != null, "origin must be provided");
        assertArgument(origin.getScheme().equals("file"), "origin must be a file URI");
        assertArgument(mode != null, "mode must be provided");

        try (
                InputStream inputStream = Files.newInputStream(Paths.get(origin));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)
        ) {
            return read(origin, bufferedInputStream, mode);
        } catch (IOException e) {
            throw new NesFileReadingException("Failed to read NES file: " + origin, e);
        } // TODO: catch illegal argument / state exceptions and wrap in NesFileReadingException
    }
    
    /**
     * Reads the input stream and deconstructs it according to header info
     * @param origin file path or url pointing to the file
     * @param inputStream caller is responsible for closing the stream
     * @param mode strictness of the reader
     */
    public Result read(URI origin, InputStream inputStream, Mode mode) throws NesFileReadingException {
        assertArgument(origin != null, "origin must be provided");
        assertArgument("file".equals(origin.getScheme()), "origin must be a file URI");
        assertArgument(inputStream != null, "inputStream must be provided");
        assertArgument(mode != null, "mode must be provided");

        var inputBuffer = readInputStream(origin, inputStream);

        final var headerSize = NesHeader.SIZE;
        var headerBuffer = inputBuffer.slice(0, headerSize);

        var headerReader = new NesHeaderReader();
        var headerResult = headerReader.read(origin, headerBuffer, mode);
        var meta = headerResult.meta();

        // FIXME: check if there is enough data before slicing. there are truncated roms out there.

        var trainerSize = meta.trainer().toBytes();
        var trainerData = trainerSize > 0
                ? inputBuffer.slice(headerSize, trainerSize)
                : ByteBuffer.allocate(0);

        var programDataSize = meta.programData().toBytes();
        var videoDataSize = meta.videoData().size().toBytes();

        var programData = inputBuffer.slice(headerSize + trainerSize, programDataSize);
        var videoData = videoDataSize > 0
                ? inputBuffer.slice(headerSize + trainerSize + programDataSize, videoDataSize)
                : ByteBuffer.allocate(0);

        int expectedDataAmount = headerSize + trainerSize + programDataSize + videoDataSize;
        int inputBufferSize = inputBuffer.capacity();

        if (inputBufferSize < expectedDataAmount) {
            throw new IllegalArgumentException("File is truncated. Expected " + expectedDataAmount + " bytes but got " + inputBufferSize);
        }

        var remainingData = inputBuffer.slice(expectedDataAmount, inputBufferSize - expectedDataAmount); // TODO: verify

        NesData data = new NesData(
                headerBuffer.rewind(),
                trainerData,
                programData,
                videoData,
                ByteBuffer.allocate(0),
                remainingData
        );

        NesFile nesFile = new NesFile(origin, meta, data, NesHash.empty());
        return new Result(nesFile, headerResult.problems());
    }

    private static @NonNull ByteBuffer readInputStream(URI origin, InputStream inputStream) {
        final byte[] inputBytes;
        try {
            inputBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new NesFileReadingException("Unable to read input bytes of: " + origin + " ", e);
        }
        var inputBuffer = ByteBuffer.wrap(inputBytes);
        inputBuffer.order(LITTLE_ENDIAN);

        if (inputBuffer.capacity() < NesHeader.SIZE) {
            throw new NesFileReadingException("Input data is too short to contain iNES header");
        }
        return inputBuffer;
    }
}
