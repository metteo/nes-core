package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.Problem;
import net.novaware.nes.core.util.Quantity;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.file.ines.NesHeader.Shared_iNES.getByte7;
import static net.novaware.nes.core.util.Quantity.ZERO_BYTES;
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

        NesHeader.Shared_iNES.Byte7 byte7 = getByte7(headerBuffer);

        byte flags8 = headerBuffer.get();
        byte flags9 = headerBuffer.get();
        byte flags10 = headerBuffer.get();

        byte[] padding = new byte[5]; // TODO: verify 0s for iNES
        headerBuffer.get(padding);


        // region Flags 7

        int mapperHi = uint(byte7.mapperHi()); // TODO: ignore hi if Archaic iNES
        int mapperLo = uint(byte6.mapper());
        short mapper = (short) (mapperHi | mapperLo);

        // endregion
        // region Flags 8

        final int programMemoryMultiplier = 8 * 1024;
        int programMemoryByte = Byte.toUnsignedInt(flags8);
        int programMemorySize = programMemoryByte == 0 // Size of PRG RAM in 8 KB units
                ? programMemoryMultiplier // Value 0 infers 8 KB for compatibility
                : programMemoryByte * programMemoryMultiplier;// TODO: uint() from chip8

        // endregion
        // region Flags 9

        NesMeta.VideoStandard videoStandard = isBitSet(flags9, 0) ? NesMeta.VideoStandard.PAL : NesMeta.VideoStandard.NTSC;

        int flag9zeroes = (flags9 & 0xFE) >> 1;
        if (flag9zeroes > 0) {
            throw new NesFileReadingException("flag9 reserved area not 0s: " + flag9zeroes); // TODO: don't throw, see note above
        }

        // endregion
        // region Flags 10

        int videoStandardBits = flags10 & 0b11;
        NesMeta.VideoStandard videoStandard2 = switch (videoStandardBits) {
            case 0 -> NesMeta.VideoStandard.NTSC;
            case 1 -> NesMeta.VideoStandard.NTSC_HYBRID;
            case 2 -> NesMeta.VideoStandard.PAL;
            case 3 -> NesMeta.VideoStandard.PAL_HYBRID; // TODO: Dendy?
            default -> NesMeta.VideoStandard.OTHER; // TODO: report a problem
        };

        boolean programMemoryPresent = !isBitSet(flags10, 4); // TODO: may conflict with the size byte, resolve

        NesMeta.ProgramMemory programMemory = programMemoryPresent
                ? new NesMeta.ProgramMemory(
                byte6.kind(),
                new Quantity(programMemorySize, Quantity.Unit.BYTES)
        )
                : new NesMeta.ProgramMemory(NesMeta.Kind.NONE, ZERO_BYTES);

        boolean busConflicts = isBitSet(flags10, 5);

        // endregion

        // NOTE: assumption for iNES, read properly in NES 2.0
        Quantity videoMemory = (videoDataSize.amount() == 0) ? new Quantity(8 * 1024, Quantity.Unit.BYTES) : ZERO_BYTES;

        NesMeta meta = NesMeta.builder()
                .title(Paths.get(origin).getFileName().toString()) // TODO: remove extension?
                .info("") // TODO: read end of the header
                .system(NesMeta.System.NES)
                .mapper(mapper)
                .busConflicts(busConflicts)
                .trainer(byte6.trainer())
                .programMemory(programMemory)
                .programData(programData)
                .videoMemory(new Quantity(videoDataSize.amount() == 0 ? 1 : 0, Quantity.Unit.BANK_8KB))
                .videoData(new NesMeta.VideoData(byte6.layout(), videoDataSize))
                .videoStandard(videoStandard2) // TODO: resolve conflicts with videoStandard variable
                .footer(new Quantity(0, Quantity.Unit.BYTES))
                .build();

        return new Result(meta, problems);
    }

    private static boolean isBitSet(byte b, int bitIndex) {
        return (b & (1 << bitIndex)) != 0;
    }
}
