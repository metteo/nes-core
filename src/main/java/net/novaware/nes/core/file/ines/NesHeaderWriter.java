package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.Problem;
import net.novaware.nes.core.util.UByteBuffer;

import java.util.List;

import static net.novaware.nes.core.file.NesMeta.System.EXTENDED;
import static net.novaware.nes.core.file.NesMeta.System.NES;
import static net.novaware.nes.core.file.NesMeta.System.PLAY_CHOICE_10;
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC;
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC_0_7;
import static net.novaware.nes.core.util.Asserts.assertArgument;

public class NesHeaderWriter extends NesHeaderHandler {

    /**
     * @param version of the resulting header
     * @param header existing buffer with correct size that will be used
     *               to write. Will be cleaned up before writing.
     * @param includeInfo should info be included at the end.
     *                    This is non-standard and may not fit in the
     *                    remaining space (will be clipped)
     */
    public record Params(
            NesFileVersion version,
            UByteBuffer header,
            boolean includeInfo // TODO: think about bitfield or enum instead
    ) {
        public Params(NesFileVersion version, boolean includeInfo) {
            this(version, NesHeader.allocate(), includeInfo);
        }
    }

    public record Result(
            UByteBuffer header,
            List<Problem> problems
    ) {
    }

    public Result write(NesMeta meta, Params params) {
        assertArguments(meta, params);

        if (params.includeInfo()) {
            // NOTE: actually applies only to DiskDude!, other shorter info strings would fit in 0.7 header
            // assertArgument(params.version == NesFileVersion.ARCHAIC, "info can be included only in archaic header"); // TODO: move to version specific method and check length
        }

        UByteBuffer header = params.header();

        // TODO: zero out the header (may have old data)

        List<NesFileVersion> versions = params.version.getHistory();

        ArchaicHeaderBuffer archaicHeader = new ArchaicHeaderBuffer(header);
        ModernHeaderBuffer modernHeader = new ModernHeaderBuffer(header);
        FutureHeaderBuffer futureHeader = new FutureHeaderBuffer(header);

        int mapper = meta.mapper();
        boolean mapperStored = false;

        if (versions.contains(ARCHAIC)) {
            archaicHeader.putMagic()
                    .putProgramData(meta.programData())
                    .putVideoData(meta.videoData().size())
                    .putVideoMemoryLayout(meta.videoData().layout())
                    .putTrainer(meta.trainer())
                    .putProgramMemoryKind(meta.programMemory().kind());


            if (archaicHeader.getMapperRange(ARCHAIC).test(mapper)) {
                archaicHeader.putMapper(ARCHAIC, mapper);
                mapperStored = true;
            }

            if (params.includeInfo()) { // TODO: maybe move it till the end?
                archaicHeader.putInfo(meta.info()); // TODO: 7-15 only
            }
        }

        if (versions.contains(ARCHAIC_0_7)) {
            if (!mapperStored && archaicHeader.getMapperRange(ARCHAIC_0_7).test(mapper)) {
                archaicHeader.putMapper(ARCHAIC_0_7, mapper);
                mapperStored = true;
            }

            // TODO: allow putting info but shorter, 8-15 only
        }

        if (versions.contains(NesFileVersion.MODERN)) {
            NesMeta.System nesOrVs = meta.system() == PLAY_CHOICE_10 || meta.system() == EXTENDED
                    ? NES // fallback to NES // TODO: report a problem that information can't be encoded
                    : meta.system();
            modernHeader.putSystem(nesOrVs);
        }

        if (versions.contains(NesFileVersion.MODERN_1_3)) {
            NesMeta.System noExtended = meta.system() == EXTENDED
                    ? NES // fallback to NES // TODO: report a problem that information can't be encoded
                    : meta.system();
            modernHeader.putSystem(noExtended)
                    .putVersion(0b00);
        }

        if (versions.contains(NesFileVersion.MODERN_1_5)) {
            modernHeader.putProgramMemory(meta.programMemory().size())
                    .putVideoStandard(meta.videoStandard()); // TODO: report a problem if video standard dual or dendy
        }

        if (versions.contains(NesFileVersion.MODERN_1_7)) {
            modernHeader.putBusConflicts(meta.busConflicts())
                    .putProgramMemoryAbsent(meta.programMemory().kind() == NesMeta.Kind.NONE)
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

        return new Result(header, List.of());
    }

    private void assertArguments(NesMeta meta, Params params) {
        assertArgument(meta != null, "meta must not be null");
        assertArgument(params != null, "params must not be null");
        assertArgument(params.version != null, "version must not be null");
        assertArgument(params.version != NesFileVersion.UNKNOWN, "version must be specified");
        assertArgument(params.header != null, "header must not be null");
    }


}
