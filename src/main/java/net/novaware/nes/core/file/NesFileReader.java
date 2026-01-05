package net.novaware.nes.core.file;

import net.novaware.nes.core.util.Quantity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class NesFileReader {

    // TODO: support both iNES and NES2.0 modes

    public NesFile read(Path path) throws IOException { // TODO: throw a business exception
        try (InputStream inputStream = Files.newInputStream(path)) { // TODO: unbuffered
            return read(path.toAbsolutePath().toString(), inputStream);
        }
    }
    
    /**
     * Reads the input stream and deconstructs it according to header info
     * @param origin file path or url pointing to the file
     * @param inputStream caller is responsible for closing the stream
     */
    public NesFile read(String origin, InputStream inputStream) throws IOException {

        final byte[] bytes = inputStream.readAllBytes();
        var fileBuffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
        var headerBuffer = fileBuffer.slice(0, 16);

        for (int i = 0; i < 4; i++) {
            byte magicPart = headerBuffer.get();

            if (magicPart != NesFile.MAGIC_BYTES[i]) {                  // TODO: optimize and improve
                throw new IllegalArgumentException("Input bytes don't follow iNES format");
            }
        }

        int programRomSize = headerBuffer.get() * 16 * 1024;
        int videoRomSize = headerBuffer.get() * 8 * 1024;

        byte flags6 = headerBuffer.get();

        // maybe BitSet?
        NesFile.Orientation nametableOrientation = (flags6 & 0b1) == 0 ? NesFile.Orientation.VERTICAL : NesFile.Orientation.HORIZONTAL;
        int trainerSize = (flags6 & 1 << 2) > 0 ? 512 : 0;
        int mapper = (flags6 & 0xF0) >> 4; // lower part for now
        var header = new NesFile.Header(
                new Quantity(trainerSize, Quantity.Unit.BYTES),
                new Quantity(programRomSize, Quantity.Unit.BYTES),
                new Quantity(videoRomSize, Quantity.Unit.BYTES),
                mapper,
                nametableOrientation
        );


        return new NesFile(origin, header, fileBuffer);
    }
}
