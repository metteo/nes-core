package net.novaware.nes.core.file;

import net.novaware.nes.core.util.Quantity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NesFile {

    /**
     * "NES\x1a"
     * `\u001a` is a SUB character, Ctrl+Z, MS-DOS eof
     */
    public static final String MAGIC = "NES\u001a";

    private final String origin;

    /**
     * Whole file buffer in read only mode
     */
    private ByteBuffer fileBuffer;

    /**
     * Whole header spliced from the file
     */
    private ByteBuffer headerBuffer;

    /**
     * Relevant info parsed from the header section
     */
    private Header header;

    /**
     * Trainer data spliced from the file
     */
    private ByteBuffer trainerBuffer;

    /**
     * PRG-ROM spliced from the file
     */
    private ByteBuffer programBuffer;

    /**
     * CHR-ROM spliced from the file
     */
    private ByteBuffer videoBuffer;

    // TODO: decide if constructor is parsing or the reader.
    /**
     * Constructs instance by deconstructing the file according to header info
     * @param origin file path or url pointing to the file
     * @param inputStream caller is responsible for closing the stream
     */
    public NesFile(String origin, InputStream inputStream) throws IOException {
        this.origin = origin;

        final byte[] bytes = inputStream.readAllBytes();
        fileBuffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
        headerBuffer = fileBuffer.slice(0, 16);

        for (int i = 0; i < 4; i++) {
            byte magicPart = headerBuffer.get();

            if (magicPart != MAGIC.getBytes(StandardCharsets.UTF_8)[i]) {                  // TODO: optimize and improve
                throw new IllegalArgumentException("Input bytes don't follow iNES format");
            }
        }

        int programRomSize = headerBuffer.get() * 16 * 1024;
        int videoRomSize = headerBuffer.get() * 8 * 1024;

        byte flags6 = headerBuffer.get();

        // maybe BitSet?
        Orientation nametableOrientation = (flags6 & 0b1) == 0 ? Orientation.VERTICAL : Orientation.HORIZONTAL;
        int trainerSize = (flags6 & 1 << 2) > 0 ? 512 : 0;
        int mapper = (flags6 & 0xF0) >> 4; // lower part for now
        header = new Header(
                new Quantity(trainerSize, Quantity.Unit.BYTES),
                new Quantity(programRomSize, Quantity.Unit.BYTES),
                new Quantity(videoRomSize, Quantity.Unit.BYTES),
                mapper,
                nametableOrientation
        );


        if (trainerSize > 0) {
            trainerBuffer = fileBuffer.slice(16, trainerSize);
        }

        programBuffer = fileBuffer.slice(16 + trainerSize, programRomSize);
        videoBuffer = fileBuffer.slice(16 + trainerSize + programRomSize, videoRomSize);
    }

    public String getOrigin() {
        return origin;
    }

    public ByteBuffer toBuffer() {
        return fileBuffer;
    }

    public Header getHeader() {
        return header;
    }

    public static class Header {
        private final Quantity trainerSize;
        private final Quantity programRomSize;
        private final Quantity videoRomSize;
        private final int mapperNumber;
        private final Orientation nametableOrientation;

        public Header(
                Quantity trainerSize,
                Quantity programRomSize,
                Quantity videoRomSize,
                int mapperNumber,
                Orientation nametableOrientation
        ) {
            this.trainerSize = trainerSize;
            this.programRomSize = programRomSize;
            this.videoRomSize = videoRomSize;
            this.mapperNumber = mapperNumber;
            this.nametableOrientation = nametableOrientation;
        }

        public Quantity getTrainerSize() {
            return trainerSize;
        }

        public Quantity getProgramRomSize() {
            return programRomSize;
        }

        public Quantity getVideoRomSize() {
            return videoRomSize;
        }

        public int getMapperNumber() {
            return mapperNumber;
        }

        public Orientation getNametableOrientation() {
            return nametableOrientation;
        }
    }

    public enum Orientation {
        VERTICAL, // 0
        HORIZONTAL, // 1
    }
}
