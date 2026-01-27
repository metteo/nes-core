package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesData;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.novaware.nes.core.util.Asserts.assertArgument;

public class NesDataOutputStream extends FilterOutputStream {

    public static final int DEFAULT_TRANSFER_BUFFER_SIZE = 8 * 1024;

    private final @Positive int transferBufferSize;

    public NesDataOutputStream(OutputStream out, @Positive int transferBufferSize) {
        super(requireNonNull(out, "out must not be null"));
        assertArgument(transferBufferSize > 0, "transferBufferSize must be positive");


        this.transferBufferSize = transferBufferSize;
    }

    public NesDataOutputStream(OutputStream out) {
        this(out, DEFAULT_TRANSFER_BUFFER_SIZE);
    }

    @SuppressWarnings("argument") // out.write(..., arg2) is not related to offset
    public void write(NesData data) throws IOException {
        assertArgument(data != null, "data must not be null");

        List<ByteBuffer> buffers = List.of(
                data.header().unwrap(),
                data.trainer(),
                data.program(),
                data.video(),
                data.misc(),
                data.footer()
        );

        byte[] transferBuffer = new byte[transferBufferSize];

        for (ByteBuffer buffer : buffers) {
            while(buffer.hasRemaining()) {

                final @NonNegative int offset = 0;
                int length = Math.min(buffer.remaining(), transferBuffer.length);

                buffer.get(transferBuffer, offset, length);
                out.write(transferBuffer, offset, length);
            }
        }
    }
}
