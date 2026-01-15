package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.ines.NesHeader.Version;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.util.Objects.requireNonNull;
import static net.novaware.nes.core.file.ines.NesHeader.SIZE;

public class NesHeaderWriter extends NesHeaderHandler {

    public record Params(
            Version version,
            boolean includeInfo // TODO: prefer enum or bitfield instead of boolean
    ) {

        public Params(Version version, boolean includeInfo) {
            this.version = requireNonNull(version);
            this.includeInfo = includeInfo;
        }
    }
    public @NonNull ByteBuffer write(Params params, NesMeta meta) {
        // TODO: validate inputs

        ByteBuffer header = ByteBuffer.allocate(SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN);

        NesHeader.Archaic_iNES.putMagic(header);
        NesHeader.Archaic_iNES.putProgramData(header, meta.programData());
        NesHeader.Archaic_iNES.putVideoData(header, meta.videoData().size());
        // TODO: throw error if archaic ines and mapper > 15?

        NesHeader.Archaic_iNES.putByte6(header, meta);

        final Version version = params.version();

        if (version == Version.ARCHAIC_iNES && params.includeInfo()) {
            NesHeader.Archaic_iNES.putInfo(header, meta.info());
        }

        if (version == Version.NES_0_7) {
            // TODO: iNES 0.7 with mapper hi only on 7th
        }

        if (version.compareTo(Version.ARCHAIC_iNES) > 0) {
            NesHeader.Shared_iNES.putByte7(header, meta, version);
        }

        if (version == Version.MODERN_iNES) {
            NesHeader.Modern_iNES.putProgramMemory(header, meta.programMemory().size());
            NesHeader.Modern_iNES.putVideoStandard(header, meta.videoStandard());
        }

        if (version == Version.UNOFFICIAL_iNES) {
            NesHeader.Unofficial_iNES.Byte10 byte10 = new NesHeader.Unofficial_iNES.Byte10(
                    meta.busConflicts(),
                    meta.programMemory().kind() != NesMeta.Kind.NONE,
                    meta.videoStandard());
            NesHeader.Unofficial_iNES.putByte10(header, byte10);
        }

        if (version == Version.NES_2_0) {
            // TODO: flags in nes 2.0
        }

        header.rewind();

        return header;
    }


}
