package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesFile;

import java.nio.ByteBuffer;
import java.util.List;

public class NesHeaderReader {

    public record Result(NesFile.Meta meta, List<NesFileReader.Problem> problems) {
    }

    public Result read(ByteBuffer header, NesFileReader.Mode mode) {
        return null;
    }
}
