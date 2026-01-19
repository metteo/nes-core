package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.NesMeta.Kind;
import net.novaware.nes.core.file.NesMeta.Layout;
import net.novaware.nes.core.file.Problem;
import net.novaware.nes.core.file.ReaderMode;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.file.NesMeta.System.NES;
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC;
import static net.novaware.nes.core.file.ines.NesFileVersion.ARCHAIC_0_7;
import static net.novaware.nes.core.file.ines.NesFileVersion.FUTURE;
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN;
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_3;
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_5;
import static net.novaware.nes.core.file.ines.NesFileVersion.MODERN_1_7;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_16KB;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_512B;
import static net.novaware.nes.core.util.Quantity.Unit.BANK_8KB;

public class NesHeaderReader extends NesHeaderHandler {

    public record Params(
            NesFileVersion version,
            ReaderMode mode // TODO: use it!
    ) {}

    public record Result(NesMeta meta, List<Problem> problems) {
    }

    public Result read(UByteBuffer header, Params params) {
        assertArgument(header != null, "header must not be null");
        assertArgument(params != null, "params must not be null");

        List<Problem> problems = new ArrayList<>();

        ArchaicHeaderBuffer archaicHeader = new ArchaicHeaderBuffer(header);
        ModernHeaderBuffer modernHeader = new ModernHeaderBuffer(header);
        FutureHeaderBuffer futureHeader = new FutureHeaderBuffer(header);

        List<NesFileVersion> versions = params.version.getHistory();

        Quantity programData = new Quantity(0, BANK_16KB);
        Quantity videoData = new Quantity(0, BANK_8KB);
        int mapper = -1;
        Layout layout = Layout.UNKNOWN;
        Quantity trainer = new Quantity(0, BANK_512B);

        String info = "";

        NesMeta.System system = NES; // TODO: default, move to sanitizer

        boolean busConflicts = false;
        Kind programMemoryKind = Kind.NONE;
        Quantity programMemorySize = new Quantity(0, BANK_8KB);
        NesMeta.VideoStandard videoStandard = NesMeta.VideoStandard.NTSC; // TODO: move to sanitizer

        if (versions.contains(ARCHAIC)) {
            archaicHeader.getMagic(); // decide if scanner should report problem or this reader?

            programData = archaicHeader.getProgramData();
            layout = archaicHeader.getVideoMemoryLayout();
            videoData = archaicHeader.getVideoData();
            mapper = archaicHeader.getMapper();
            trainer = archaicHeader.getTrainer();
            programMemoryKind = archaicHeader.getProgramMemoryKind();

            // TODO: read info?
            if (params.version == ARCHAIC) {
                info = archaicHeader.getInfo();
            }
        }

        if (versions.contains(ARCHAIC_0_7)) {
            // FIXME: wtf? why modern?
            mapper = modernHeader.getMapper(); // TODO: do we want to set the mapper 3 times (archaic, 0.7, 2.0)? possibly other fields too.

            // TODO: read shorter info?
        }

        if (versions.contains(MODERN)) {
            system = modernHeader.getSystem(); // TODO: except playchoice...
        }

        if (versions.contains(MODERN_1_3)) {
            system = modernHeader.getSystem(); // TODO: with playchoice...
            modernHeader.getVersion(); // TODO: what to do with that? scanner already used it?
        }

        if (versions.contains(MODERN_1_5)) {
            programMemorySize = modernHeader.getProgramMemory();
            videoStandard = modernHeader.getVideoStandard();
        }

        if (versions.contains(MODERN_1_7)) {
            busConflicts = modernHeader.getBusConflicts();
            programMemoryKind = modernHeader.getProgramMemoryPresent() ? programMemoryKind : Kind.NONE; // TODO: will clear battery :(
            videoStandard = modernHeader.getVideoStandardExt(); // TODO: overrides previous!
        }

        if (versions.contains(FUTURE)) {
            futureHeader.unwrap();
            throw new IllegalArgumentException("NES 2.0 is not yet supported");
        }


        @Unsigned byte[] padding = new byte[5]; // TODO: verify 0s for iNES
        header.get(padding);

        header.rewind();

        NesMeta meta = NesMeta.builder()
                .title("") // NOTE: not available at this point
                .info(info)
                .system(system)
                .mapper(mapper)
                .busConflicts(busConflicts)
                .trainer(trainer)
                .programMemory(new NesMeta.ProgramMemory(
                        programMemorySize.amount() == 0
                                ? Kind.NONE
                                : programMemoryKind,
                        programMemorySize))
                .programData(programData)
                .videoMemory(new Quantity(videoData.amount() == 0 ? 1 : 0, BANK_8KB)) // TODO: move defaults to Sanitizer
                .videoData(new NesMeta.VideoData(layout, videoData))
                .videoStandard(videoStandard)
                .footer(new Quantity(0, Quantity.Unit.BYTES))
                .build();

        return new Result(meta, problems);
    }
}
