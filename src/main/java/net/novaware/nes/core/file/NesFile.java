package net.novaware.nes.core.file;

import net.novaware.nes.core.util.Quantity;

import java.io.InputStream;
import java.nio.ByteBuffer;

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
    public NesFile(String origin, InputStream inputStream) {
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    public ByteBuffer toBuffer() {
        return fileBuffer;
    }

    public static class Header {
        private int programRom; // TODO: length, with unit?
        private Quantity programRom2;
        private int videoRom;
        private int mapper; // TODO: enum for supported?
        private int nameTableArrangement; // TODO: enum
        private boolean trainerPresent;
        private boolean nes_v2_0;
        private String comment;
    }
}
