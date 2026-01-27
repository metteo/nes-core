package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.ReaderMode;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static net.novaware.nes.core.util.Asserts.assertArgument;

public class NesFileFinder {

    private static final System.Logger logger = System.getLogger(NesFileFinder.class.getSimpleName());

    private final Path basePath;
    private final Path summaryFile;

    private final NesFileReader reader = new NesFileReader();

    public NesFileFinder(Path basePath) {
        this.basePath = basePath;

        Path parentDir = basePath.getParent();
        summaryFile = (parentDir == null ? basePath : parentDir).resolve("summary.csv");
    }

    public void find() {
        try {
            if (Files.notExists(summaryFile)) {
                Files.createFile(summaryFile);
            }
            Files.writeString(summaryFile, "", WRITE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        find(basePath);
    }

    public void find(Path path) {
        if (Files.isDirectory(path)) {
            logger.log(TRACE, () -> "Scanning: " + path + " ...");
            try {
                Files.list(path)
                        .forEach(this::find);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else if (Files.isRegularFile(path)) {
            scan(path);
        }
    }

    private void scan(Path path) {
        logger.log(TRACE, "File: " + path + " ...");

        NesFileReader.Result result = reader.read(path.toUri(), ReaderMode.LENIENT);

        if (!result.problems().isEmpty() || result.nesFile() == null) {
            logger.log(ERROR, () -> path + ": " + result.problems());
        } else {
            NesFile nesFile = result.nesFile();
            StringBuilder builder = toTabulatedLine(path, nesFile);

            try {
                Files.writeString(summaryFile, builder, WRITE, APPEND);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        }
    }

    private @NonNull StringBuilder toTabulatedLine(Path path, NesFile nesFile) {
        NesMeta meta = nesFile.meta();

        StringBuilder buffer = new StringBuilder();
        buffer.append(path.subpath(path.getNameCount() - 2, path.getNameCount())).append("\t");
        buffer.append(meta.title()).append("\t");
        buffer.append(meta.system()).append("\t");
        buffer.append(meta.mapper()).append("\t");
        buffer.append(meta.programMemory().kind()).append("\t");
        buffer.append(meta.programMemory().size().toBytes()).append(" B").append("\t");
        buffer.append(meta.trainer().toBytes()).append(" B").append("\t");
        buffer.append(meta.programData().toBytes()).append(" B").append("\t");
        buffer.append(meta.videoData().size().toBytes()).append(" B").append("\t");
        buffer.append(meta.videoData().layout()).append("\t");
        buffer.append(meta.videoStandard()).append("\t");
        buffer.append(meta.info()).append(System.lineSeparator());

        return buffer;
    }

    @SuppressWarnings("array.access.unsafe.high.constant") // checker doesn't detect that assertArg will throw
    static void main(String... args) {
        assertArgument(args.length > 0, "args must have 1 element");

        new NesFileFinder(Path.of(args[0])).find();
    }
}
