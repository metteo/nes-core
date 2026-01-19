package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.util.UByteBuffer;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static net.novaware.nes.core.util.Asserts.assertArgument;

public class NesHeaderWriter extends NesHeaderHandler {

    public record Params(
            NesFileVersion version,
            boolean includeInfo // TODO: think about bitfield or enum instead
    ) {

    }
    public @NonNull UByteBuffer write(NesMeta meta, Params params) {
        assertArgument(meta != null, "meta must not be null");
        assertArgument(params != null, "params must not be null");
        assertArgument(params.version != null, "version must not be null");
        assertArgument(params.version != NesFileVersion.UNKNOWN, "version must be specified");
        if (params.includeInfo()) {
            // NOTE: actually applies only to DiskDude!, other shorter info strings would fit in 0.7 header
            // assertArgument(params.version == NesFileVersion.ARCHAIC, "info can be included only in archaic header"); // TODO: move to version specific method and check length
        }

        UByteBuffer header = NesHeader.allocate();

        List<NesFileVersion> versions = params.version.getHistory();

        ArchaicHeaderBuffer archaicHeader = new ArchaicHeaderBuffer(header);
        ModernHeaderBuffer modernHeader = new ModernHeaderBuffer(header);
        FutureHeaderBuffer futureHeader = new FutureHeaderBuffer(header);

        int mapper = meta.mapper();
        boolean mapperStored = false;

        if (versions.contains(NesFileVersion.ARCHAIC)) {
            archaicHeader.putMagic()
                    .putProgramData(meta.programData())
                    .putVideoData(meta.videoData().size())
                    .putVideoMemoryLayout(meta.videoData().layout())
                    .putTrainer(meta.trainer())
                    .putProgramMemoryKind(meta.programMemory().kind());


            if (archaicHeader.getMapperRange().test(mapper)) {
                archaicHeader.putMapper(mapper);
                mapperStored = true;
            }

            if (params.includeInfo()) { // TODO: maybe move it till the end?
                archaicHeader.putInfo(meta.info()); // TODO: 7-15 only
            }
        }

        if (versions.contains(NesFileVersion.ARCHAIC_0_7)) {

            // FIXME: wtf? why modern?
            if (!mapperStored && modernHeader.getMapperRange().test(mapper)) {
                modernHeader.putMapper(mapper);
                mapperStored = true;
            }

            // TODO: allow putting info but shorter, 8-15 only
        }

        if (versions.contains(NesFileVersion.MODERN)) {
            modernHeader.putSystem(meta.system()); // TODO: except playchoice
        }

        if (versions.contains(NesFileVersion.MODERN_1_3)) {
            modernHeader.putSystem(meta.system()) // TODO: including playchoice
                    .putVersion(0b00);
        }

        if (versions.contains(NesFileVersion.MODERN_1_5)) {
            modernHeader.putProgramMemory(meta.programMemory().size())
                    .putVideoStandard(meta.videoStandard());
        }

        if (versions.contains(NesFileVersion.MODERN_1_7)) {
            modernHeader.putBusConflicts(meta.busConflicts())
                    .putProgramMemoryPresent(meta.programMemory().kind() != NesMeta.Kind.NONE)
                    .putVideoStandardExt(meta.videoStandard());
        }

        if (versions.contains(NesFileVersion.FUTURE)) {
            futureHeader.unwrap();
            throw new IllegalArgumentException("NES 2.0 is not yet supported");
        }

        if (!mapperStored) {
            throw new IllegalStateException("none of the buffers accepted the mapper: " + mapper);
        }

        header.rewind();

        return header;
    }


}
