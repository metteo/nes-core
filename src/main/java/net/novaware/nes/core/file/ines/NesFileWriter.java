package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesFile;
import net.novaware.nes.core.file.NesFile.Data;
import net.novaware.nes.core.file.NesFile.Meta;
import net.novaware.nes.core.file.ines.NesFileHeader.Archaic_iNES;
import net.novaware.nes.core.file.ines.NesFileHeader.Shared_iNES;
import net.novaware.nes.core.util.Quantity;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static net.novaware.nes.core.util.Asserts.assertArgument;

public class NesFileWriter extends NesFileHandler {

    public ByteBuffer write(NesFile nesFile, Version version) {
        assertArgument(nesFile != null, "nesFile must not be null");
        assertArgument(version != null, "version must not be null");

        Meta meta = nesFile.meta();

        // TODO: writer should use data.header instead. Converter will update / create header from Meta
        ByteBuffer header = writeHeader(version, meta);

        Data data = nesFile.data();

        // TODO: consider using data buffer capacities for all below or more, validate meta vs data sizes
        int trainerSize = toBytes(meta.trainer());
        int prgSize = toBytes(meta.programData());
        int chrSize = toBytes(meta.videoData());

        int totalSize = header.capacity() + trainerSize + prgSize + chrSize; // base
        totalSize += data.inst().capacity(); // 0 or 8KB
        totalSize += data.prom().capacity(); // 0 or 2x16B
        totalSize += data.remainder().capacity(); // 0 or more

        ByteBuffer out = ByteBuffer.allocate(totalSize);
        out.order(ByteOrder.LITTLE_ENDIAN);

        putBuffer(out, header);
        putBuffer(out, data.trainer());
        putBuffer(out, data.program());
        putBuffer(out, data.video());
        putBuffer(out, data.inst());
        putBuffer(out, data.prom());
        putBuffer(out, data.remainder());

        return out.flip();
    }

    private @NonNull ByteBuffer writeHeader(Version version, Meta meta) {
        ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN);

        Archaic_iNES.putMagic(header);
        Archaic_iNES.putProgramData(header, meta.programData());
        Archaic_iNES.putVideoData(header, meta.videoData());
        // TODO: throw error if archaic ines and mapper > 15?

        Archaic_iNES.putFlag6(header, meta);

        if (version == Version.ARCHAIC_iNES) {
            Archaic_iNES.putInfo(header, meta.info());
        }

        if (version == Version.NES_0_7) {
            // TODO: iNES 0.7 with mapper hi only on 7th
        }

        if (version.compareTo(Version.ARCHAIC_iNES) > 0) {
            Shared_iNES.putFlag7(header, meta, version);
        }

        if (version == Version.MODERN_iNES) {
            NesFileHeader.Modern_iNES.putProgramMemory(header, meta.programMemory());
            NesFileHeader.Modern_iNES.putVideoStandard(header, meta.videoStandard());
        }

        if (version == Version.UNOFFICIAL_iNES) {
            // TODO: flag10
        }

        if (version == Version.NES_2_0) {
            // TODO: flag8 in nes 2.0
        }

        // 10-15: Padding
        for (int i = 0; i < 6; i++) {
            header.put((byte) 0);
        }

        header.flip();

        return header;
    }

    private int toBytes(Quantity quantity) {
        if (quantity == null) { return 0; }
        return quantity.toBytes();
    }

    private void putBuffer(ByteBuffer dst, ByteBuffer src) {
        ByteBuffer dup = src.duplicate();
        dst.put(dup);
    }
}
