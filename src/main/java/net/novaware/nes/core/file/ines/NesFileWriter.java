package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesData;
import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static net.novaware.nes.core.util.Asserts.assertArgument;

/**
 * Writer writes what is inside data section. If some changes are needed e.g. in header / footer process
 * it with a different tool.
 */
public class NesFileWriter extends NesFileHandler {

    public ByteBuffer write(NesFile nesFile, NesFileVersion version) { // TODO: remove the version. write header as is.
        assertArgument(nesFile != null, "nesFile must not be null");
        assertArgument(version != null, "version must not be null");

        NesMeta meta = nesFile.meta();

        // TODO: writer should use data.header instead. Converter will update / create header from Meta
        UByteBuffer header = new NesHeaderWriter().write(new NesHeaderWriter.Params(version, true), meta);

        NesData data = nesFile.data();

        // TODO: consider using data buffer capacities for all below or more, validate meta vs data sizes
        int trainerSize = toBytes(meta.trainer());
        int prgSize = toBytes(meta.programData());
        int chrSize = toBytes(meta.videoData().size());

        int totalSize = header.capacity() + trainerSize + prgSize + chrSize; // base
        totalSize += data.misc().capacity(); // 0 or 8KB+2x16B
        totalSize += data.footer().capacity(); // 0 or more

        ByteBuffer out = ByteBuffer.allocate(totalSize);
        out.order(ByteOrder.LITTLE_ENDIAN);

        putBuffer(out, header.unwrap());
        putBuffer(out, data.trainer());
        putBuffer(out, data.program());
        putBuffer(out, data.video());
        putBuffer(out, data.misc());
        putBuffer(out, data.footer());

        return out.flip();
    }

    private void putBuffer(ByteBuffer dst, ByteBuffer src) {
        ByteBuffer dup = src.duplicate();
        dst.put(dup);
    }

    private int toBytes(Quantity quantity) {
        if (quantity == null) { return 0; }
        return quantity.toBytes();
    }
}
