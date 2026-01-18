package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.ines.NesHeader.Version;
import net.novaware.nes.core.util.UByteBuffer;
import org.jspecify.annotations.NonNull;

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
    public @NonNull UByteBuffer write(Params params, NesMeta meta) {
        // TODO: validate inputs

        UByteBuffer header = UByteBuffer.allocate(SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN);

        ArchaicHeaderBuffer archaicHeader = new ArchaicHeaderBuffer(header);
        ModernHeaderBuffer modernHeader = new ModernHeaderBuffer(header);

        archaicHeader.putMagic()
                .putProgramData(meta.programData())
                .putVideoData(meta.videoData().size())
                .putMapper(meta.mapper() & 0xF) // TODO: temporary, modern header buffer will accept bigger range.
                .putMemoryLayout(meta.videoData().layout())
                .putTrainer(meta.trainer())
                .putMemoryKind(meta.programMemory().kind());

        final Version version = params.version();

        if (version == Version.ARCHAIC_iNES && params.includeInfo()) {
            archaicHeader.putInfo(meta.info());
        }

        if (version == Version.NES_0_7) {
            // TODO: iNES 0.7 with mapper hi only on 7th
        }

        if (version.compareTo(Version.ARCHAIC_iNES) > 0) {
            modernHeader.putSystem(meta.system())
                    .putVersion(0b00)
                    .putMapper(meta.mapper());
        }

        if (version == Version.MODERN_iNES) {
            modernHeader.putProgramMemory(meta.programMemory().size())
                    .putVideoStandard(meta.videoStandard());
        }

        if (version == Version.UNOFFICIAL_iNES) {
            modernHeader.putBusConflicts(meta.busConflicts())
                    .putProgramMemoryPresent(meta.programMemory().kind() != NesMeta.Kind.NONE)
                    .putVideoStandardExt(meta.videoStandard());
        }

        if (version == Version.NES_2_0) {
            // TODO: flags in nes 2.0
        }

        header.rewind();

        return header;
    }


}
