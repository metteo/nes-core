package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.NesMeta.Kind;
import net.novaware.nes.core.file.Problem;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.file.ines.NesHeader.Modern_iNES.BYTE_8;

public class NesHeaderReader extends NesHeaderHandler {

    private final NesHeader.Version version;

    public NesHeaderReader(NesHeader.Version version) {
        this.version = version;
    }

    public record Result(NesMeta meta, List<Problem> problems) {
    }

    public Result read(@NonNull URI origin, UByteBuffer headerBuffer, NesFileReader.Mode mode) {
        List<Problem> problems = new ArrayList<>();

        headerBuffer.position(4); // TODO: temporary, use indexed methods

        ArchaicHeaderBuffer archaicHeader = new ArchaicHeaderBuffer(headerBuffer);
        ModernHeaderBuffer modernHeader = new ModernHeaderBuffer(headerBuffer);

        Quantity programData = archaicHeader.getProgramData();
        Quantity videoDataSize = archaicHeader.getVideoData();
        int mapper = archaicHeader.getMapper();
        Quantity trainer = archaicHeader.getTrainer();
        NesMeta.Layout layout = archaicHeader.getMemoryLayout();
        NesMeta.Kind kind = archaicHeader.getMemoryKind();

        Quantity programMemorySize              = null;
        NesMeta.VideoStandard videoStandard     = null;
        NesHeader.Unofficial_iNES.Byte10 byte10 = null;

        String info = "";

        if (version.compareTo(NesHeader.Version.ARCHAIC_iNES) > 0) { // TODO: if-else depending on version is very bad, improve
            mapper = modernHeader.getMapper();
            headerBuffer.position(BYTE_8); // TODO: temporary until all is buffer
            programMemorySize              = NesHeader.Modern_iNES.getProgramMemory(headerBuffer); // TODO: report as problem to fix instead of defaulting to 1
            videoStandard     = NesHeader.Modern_iNES.getVideoStandard(headerBuffer);
            byte10 = NesHeader.Unofficial_iNES.getByte10(headerBuffer);
        } else {
            programMemorySize = new Quantity(0, Quantity.Unit.BANK_8KB);
            videoStandard = NesMeta.VideoStandard.NTSC;
            byte10 = new NesHeader.Unofficial_iNES.Byte10(false, false, NesMeta.VideoStandard.NTSC);

            info = archaicHeader.getInfo();
        }


        @Unsigned byte[] padding = new byte[5]; // TODO: verify 0s for iNES
        headerBuffer.get(padding);

        headerBuffer.rewind();

        NesMeta meta = NesMeta.builder()
                .title(toTitle(origin)) // TODO: remove extension?
                .info(info)
                .system(NesMeta.System.NES)
                .mapper(mapper)
                .busConflicts(byte10.busConflicts())
                .trainer(trainer)
                .programMemory(new NesMeta.ProgramMemory(programMemorySize.amount() == 0 ? Kind.NONE : kind, programMemorySize))
                .programData(programData)
                .videoMemory(new Quantity(videoDataSize.amount() == 0 ? 1 : 0, Quantity.Unit.BANK_8KB))
                .videoData(new NesMeta.VideoData(layout, videoDataSize))
                .videoStandard(videoStandard)
                .footer(new Quantity(0, Quantity.Unit.BYTES))
                .build();

        return new Result(meta, problems);
    }

    private @NonNull String toTitle(@NonNull URI origin) {
        Path fileName = Path.of(origin).getFileName();

        return fileName != null ? fileName.toString() : "";
    }
}
