package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static net.novaware.nes.core.file.ines.NesHeader.SIZE;

public class NesHeaderWriter extends NesHeaderHandler {

    public @NonNull ByteBuffer write(NesHeader.Version version, NesMeta meta) {
        ByteBuffer header = ByteBuffer.allocate(SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN);

        NesHeader.Archaic_iNES.putMagic(header);
        NesHeader.Archaic_iNES.putProgramData(header, meta.programData());
        NesHeader.Archaic_iNES.putVideoData(header, meta.videoData().size());
        // TODO: throw error if archaic ines and mapper > 15?

        NesHeader.Archaic_iNES.putByte6(header, meta);

        if (version == NesHeader.Version.ARCHAIC_iNES) {
            NesHeader.Archaic_iNES.putInfo(header, meta.info());
        }

        if (version == NesHeader.Version.NES_0_7) {
            // TODO: iNES 0.7 with mapper hi only on 7th
        }

        if (version.compareTo(NesHeader.Version.ARCHAIC_iNES) > 0) {
            NesHeader.Shared_iNES.putByte7(header, meta, version);
        }

        if (version == NesHeader.Version.MODERN_iNES) {
            NesHeader.Modern_iNES.putProgramMemory(header, meta.programMemory().size());
            NesHeader.Modern_iNES.putVideoStandard(header, meta.videoStandard());
        }

        if (version == NesHeader.Version.UNOFFICIAL_iNES) {
            NesHeader.Unofficial_iNES.Byte10 byte10 = new NesHeader.Unofficial_iNES.Byte10(
                    meta.busConflicts(),
                    meta.programMemory().kind() != NesMeta.Kind.NONE,
                    meta.videoStandard());
            NesHeader.Unofficial_iNES.putByte10(header, byte10);
        }

        if (version == NesHeader.Version.NES_2_0) {
            // TODO: flags in nes 2.0
        }

        // 10-15: Padding
        for (int i = 0; i < 6; i++) {
            header.put((byte) 0);
        }

        header.flip();

        return header;
    }


}
