package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesData;
import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesHash;
import net.novaware.nes.core.file.Problem;
import net.novaware.nes.core.file.ReaderMode;
import net.novaware.nes.core.util.UByteBuffer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.novaware.nes.core.file.Problem.Severity.MAJOR;
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

    public record Result(@Nullable NesFile nesFile, List<Problem> problems) {
    }

    // TODO: add method for parsing header and returning just meta

    // TODO: allow specifying the version (user override)
    public Result read(URI origin, ReaderMode mode) throws NesFileReadingException { // TODO: test
        assertArgument(origin != null, "origin must be provided");
        assertArgument(origin.getScheme().equals("file"), "origin must be a file URI");
        assertArgument(mode != null, "mode must be provided");

        try (
                InputStream inputStream = Files.newInputStream(Path.of(origin));
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
    public Result read(URI origin, InputStream inputStream, ReaderMode mode) throws NesFileReadingException {
        assertArgument(origin != null, "origin must be provided");
        assertArgument("file".equals(origin.getScheme()), "origin must be a file URI");
        assertArgument(inputStream != null, "inputStream must be provided");
        assertArgument(mode != null, "mode must be provided");

        var allProblems = new ArrayList<Problem>();

        var inputBuffer = readInputStream(origin, inputStream);

        final var headerSize = NesHeader.SIZE;
        var headerBuffer = UByteBuffer.of(inputBuffer.slice(0, headerSize));

        var headerScanner = new NesHeaderScanner();
        var headerScanResult = headerScanner.scan(headerBuffer);

        if (headerScanResult.version() == NesFileVersion.FUTURE) {
            return new Result(null, List.of(new Problem(MAJOR, "NES 2.0 is not yet supported")));
        }

        // TODO: report if not GAME_NES

        var headerReader = new NesHeaderReader();
        var headerReaderParams = new NesHeaderReader.Params(headerScanResult.version(), mode);
        var headerResult = headerReader.read(headerBuffer, headerReaderParams);
        var meta = headerResult.meta();

        allProblems.addAll(headerScanResult.problems());
        allProblems.addAll(headerResult.problems());

        // FIXME: check if there is enough data before slicing. there are truncated roms out there.

        var trainerSize = meta.trainer().toBytes();
        var programDataSize = meta.programData().toBytes();
        var videoDataSize = meta.videoData().size().toBytes();

        int expectedDataAmount = headerSize + trainerSize + programDataSize + videoDataSize;
        int inputBufferSize = inputBuffer.capacity();

        if (inputBufferSize < expectedDataAmount) {
            allProblems.add(new Problem(MAJOR, "File is truncated. Expected " + expectedDataAmount + " bytes but got " + inputBufferSize));
            return new Result(null, allProblems);
        }

        var trainerData = trainerSize > 0
                ? inputBuffer.slice(headerSize, trainerSize)
                : ByteBuffer.allocate(0);
        var programData = inputBuffer.slice(headerSize + trainerSize, programDataSize);
        var videoData = videoDataSize > 0
                ? inputBuffer.slice(headerSize + trainerSize + programDataSize, videoDataSize)
                : ByteBuffer.allocate(0);

        int remainingDataSize = inputBufferSize - expectedDataAmount;
        var remainingData = inputBuffer.slice(expectedDataAmount, remainingDataSize);

        NesData data = new NesData(
                headerBuffer.rewind(),
                trainerData,
                programData,
                videoData,
                ByteBuffer.allocate(0),
                remainingData
        );

        var metaWithMaybeTitle = new NesFooterReader().read(remainingData, meta); // TODO: or default to file name without extension toTitle(URI)

        NesFile nesFile = new NesFile(origin, metaWithMaybeTitle, data, NesHash.empty());
        return new Result(nesFile, allProblems);
    }

    private @NonNull String toTitle(@NonNull URI origin) {
        Path fileName = Path.of(origin).getFileName();

        return fileName != null ? fileName.toString() : "";
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
