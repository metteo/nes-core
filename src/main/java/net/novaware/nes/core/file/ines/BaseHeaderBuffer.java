package net.novaware.nes.core.file.ines;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.util.Asserts.assertArgument;

public abstract class BaseHeaderBuffer {

    protected final ByteBuffer header;

    public BaseHeaderBuffer(ByteBuffer header) {
        assertArgument(header != null, "header cannot be null");
        assertArgument(header.capacity() == NesHeader.SIZE, () -> "header must be " + NesHeader.SIZE + " bytes");

        this.header = header;
    }

    public ByteBuffer unwrap() {
        return header;
    }
}
