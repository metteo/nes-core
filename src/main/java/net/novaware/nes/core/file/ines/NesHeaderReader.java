package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta;
import net.novaware.nes.core.file.ines.NesHeader.Version;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Quantity;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.file.ines.NesFileReader.Severity.MAJOR;
import static net.novaware.nes.core.file.ines.NesFileReader.Severity.MINOR;
import static net.novaware.nes.core.file.ines.NesHeader.Archaic_iNES.getMagic;
import static net.novaware.nes.core.file.ines.NesHeader.Shared_iNES.BYTE_7;
import static net.novaware.nes.core.file.ines.NesHeader.Shared_iNES.getByte7;
import static net.novaware.nes.core.util.Quantity.ZERO_BYTES;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class NesHeaderReader extends NesHeaderHandler {

    public record Result(NesMeta meta, List<NesFileReader.Problem> problems) {
    }

    public Result read(URI origin, ByteBuffer headerBuffer, NesFileReader.Mode mode) {
        List<NesFileReader.Problem> problems = new ArrayList<>();

        readMagicNumber(problems, headerBuffer);

        Quantity programData = NesHeader.Archaic_iNES.getProgramData(headerBuffer);
        Quantity videoDataSize = NesHeader.Archaic_iNES.getVideoData(headerBuffer);
        NesHeader.Archaic_iNES.Byte6 byte6 = NesHeader.Archaic_iNES.getByte6(headerBuffer);

        NesHeader.Shared_iNES.Byte7 byte7 = getByte7(headerBuffer);

        headerBuffer.position(BYTE_7); // TODO: detect version in stream instead of jumping around?
        Version version = detectVersion(headerBuffer);
        headerBuffer.position(8); // TODO: define constant

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

    Version detectVersion(ByteBuffer header) {
        int versionBits = getByte7(header).versionBits();

        byte[] bytes12to15 = new byte[4];
        header.get(12, bytes12to15);

        if (versionBits == 0b10) { // TODO: & size taking into account byte 9 does not exceed the actual size of the ROM image
            return Version.NES_2_0;
        }

        if (versionBits == 0b00 && allZeros(bytes12to15)) {
            return Version.MODERN_iNES;
        }

        byte[] bytes7to15 = new byte[9];
        header.get(7, bytes7to15);
        String maybeDiskDude = new String(bytes7to15);

        if (maybeDiskDude.equals("DiskDude!") || versionBits == 0b01) { // full string or just part of D
            return Version.ARCHAIC_iNES;
        }

        return Version.NES_0_7;
    }

    private boolean allZeros(byte[] bytes) {
        for (byte b : bytes) {
            if (uint(b) != 0) { return false; }
        }

        return true;
    }



    /* package */ static void readMagicNumber(List<NesFileReader.Problem> problems, ByteBuffer headerBuffer) {
        byte[] fourBytes = getMagic(headerBuffer);

        int matchPercent = NesHeader.Archaic_iNES.MAGIC_NUMBER.matchesPartially(fourBytes);
        assert matchPercent >= 0 && matchPercent <= 100 : "wrap percentage in a record"; // TODO: do it
        if (0 <= matchPercent && matchPercent < 75) {
            problems.add(new NesFileReader.Problem(MAJOR, "Less than 75% of magic number is matching: " + Hex.s(fourBytes)));
        } else if (75 <= matchPercent && matchPercent < 100) {
            problems.add(new NesFileReader.Problem(MINOR, "More than 75% of magic number is matching: " + Hex.s(fourBytes)));
        }
    }

    private static boolean isBitSet(byte b, int bitIndex) {
        return (b & (1 << bitIndex)) != 0;
    }
}
