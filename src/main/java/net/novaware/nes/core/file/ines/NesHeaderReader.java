package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.Problem;
import net.novaware.nes.core.util.Quantity;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class NesHeaderReader extends NesHeaderHandler {

    private final NesHeader.Version version;

    public NesHeaderReader(NesHeader.Version version) {
        this.version = version;
    }

    public record Result(NesMeta meta, List<Problem> problems) {
    }

    public Result read(URI origin, ByteBuffer headerBuffer, NesFileReader.Mode mode) {
        List<Problem> problems = new ArrayList<>();

        headerBuffer.position(4); // TODO: temporary, use indexed methods

        Quantity programData = NesHeader.Archaic_iNES.getProgramData(headerBuffer);
        Quantity videoDataSize = NesHeader.Archaic_iNES.getVideoData(headerBuffer);
        NesHeader.Archaic_iNES.Byte6 byte6 = NesHeader.Archaic_iNES.getByte6(headerBuffer);
        NesHeader.Shared_iNES.Byte7 byte7 = NesHeader.Shared_iNES.getByte7(headerBuffer);
        Quantity programMemorySize = NesHeader.Modern_iNES.getProgramMemory(headerBuffer);
        NesMeta.VideoStandard videoStandard = NesHeader.Modern_iNES.getVideoStandard(headerBuffer);
        NesHeader.Unofficial_iNES.Byte10 byte10 = NesHeader.Unofficial_iNES.getByte10(headerBuffer);

        byte[] padding = new byte[5]; // TODO: verify 0s for iNES
        headerBuffer.get(padding);


        int mapperHi = uint(byte7.mapperHi()); // TODO: ignore hi if Archaic iNES
        int mapperLo = uint(byte6.mapper());
        short mapper = (short) (mapperHi | mapperLo);

        NesMeta meta = NesMeta.builder()
                .title(Paths.get(origin).getFileName().toString()) // TODO: remove extension?
                .info("") // TODO: read end of the header
                .system(NesMeta.System.NES)
                .mapper(mapper)
                .busConflicts(byte10.busConflicts())
                .trainer(byte6.trainer())
                .programMemory(new NesMeta.ProgramMemory(byte6.kind(), programMemorySize))
                .programData(programData)
                .videoMemory(new Quantity(videoDataSize.amount() == 0 ? 1 : 0, Quantity.Unit.BANK_8KB))
                .videoData(new NesMeta.VideoData(byte6.layout(), videoDataSize))
                .videoStandard(videoStandard)
                .footer(new Quantity(0, Quantity.Unit.BYTES))
                .build();

        return new Result(meta, problems);
    }
}
