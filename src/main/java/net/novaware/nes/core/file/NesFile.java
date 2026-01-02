package net.novaware.nes.core.file;

import net.novaware.nes.core.util.Quantity;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NesFile {

    /**
     * "NES\x1a"
     * `\u001a` is a SUB character, Ctrl+Z, MS-DOS eof
     */
    public static final String MAGIC_STRING = "NES\u001a";
    public static final byte[] MAGIC_BYTES = MAGIC_STRING.getBytes(UTF_8); // FIXME: mutable!

    public static final int HEADER_SIZE = 16;

    public static class Header { // TODO: make mutable to allow construction? or builder?
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

    public NesFile(
            String origin,
            Header header,
            ByteBuffer fileBuffer
    ) {
        this.origin = origin;
        this.header = header;
        this.fileBuffer = fileBuffer;
    }

    private String origin;

    /**
     * Relevant info parsed from the header section
     */
    private Header header;

    /**
     * Whole file buffer in read only mode
     */
    private ByteBuffer fileBuffer;

    public String getOrigin() {
        return origin;
    }

    public ByteBuffer toBuffer() {
        return fileBuffer;
    }

    public Header getHeader() {
        return header;
    }

    /**
     * Whole header spliced from the file
     */
    public ByteBuffer getHeaderBuffer() { // FIXME: inefficient if call multiple times
        return fileBuffer.slice(0, 16);
    }

    /**
     * Trainer data spliced from the file if available
     */
    public ByteBuffer getTrainerBuffer() {
        final int trainerSize = header.getTrainerSize().getAmount(); // TODO: hard assert the unit to be sure
        if (trainerSize > 0) {
            return fileBuffer.slice(16, trainerSize);
        } else {
            return ByteBuffer.allocate(0);
        }
    }

    /**
     * PRG-ROM spliced from the file
     */
    public ByteBuffer getProgramRomBuffer() {
        final int trainerSize = header.getTrainerSize().getAmount();
        final int programRomSize = header.getProgramRomSize().getAmount();

        return fileBuffer.slice(HEADER_SIZE + trainerSize, programRomSize);
    }

    /**
     * CHR-ROM spliced from the file
     */
    public ByteBuffer getVideoRomBuffer() {
        final int trainerSize = header.getTrainerSize().getAmount();
        final int programRomSize = header.getProgramRomSize().getAmount();
        final int videoRomSize = header.getVideoRomSize().getAmount();

        return fileBuffer.slice(16 + trainerSize + programRomSize, videoRomSize);
    }
}
