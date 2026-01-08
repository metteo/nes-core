package net.novaware.nes.core.file;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.util.Asserts.assertArgument;

public class NesFileWriter extends NesFileHandler {

    public ByteBuffer write(NesFile nesFile, NesFileHandler.Version version) {
        assertArgument(nesFile != null, "nesFile must not be null");
        assertArgument(version != null, "version must not be null");

        throw new RuntimeException("not implemented");
    }
}
