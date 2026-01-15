package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class NesFooterWriter extends NesFooterHandler {

    public ByteBuffer write(NesMeta meta) {
        final String title = meta.title();
        final int size = meta.footer().toBytes();

        return write(title, size);
    }

    public ByteBuffer write(String title, int size) {
        return (title.isEmpty() || size == 0)
            ? ByteBuffer.allocate(0)
            : ByteBuffer.allocate(size)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(title.getBytes(StandardCharsets.US_ASCII)) // TODO: move to NesFooterWriter
                .rewind();
    }
}
