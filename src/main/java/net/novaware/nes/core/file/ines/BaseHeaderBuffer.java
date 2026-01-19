package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.util.UByteBuffer;

import static net.novaware.nes.core.util.Asserts.assertArgument;

public abstract class BaseHeaderBuffer {

    protected final UByteBuffer header;

    public BaseHeaderBuffer(UByteBuffer header) {
        assertArgument(header != null, "header cannot be null");
        assertArgument(header.capacity() == NesHeader.SIZE, () -> "header must be " + NesHeader.SIZE + " bytes");

        this.header = header;
    }

    public UByteBuffer unwrap() {
        return header;
    }
}
