package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.MagicNumber;
import net.novaware.nes.core.file.Problem;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static net.novaware.nes.core.file.Problem.Severity.MAJOR;
import static net.novaware.nes.core.file.Problem.Severity.MINOR;
import static net.novaware.nes.core.file.ines.ArchaicHeaderBuffer.getMagic;
import static net.novaware.nes.core.file.ines.ModernHeaderBuffer.getVersion;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

/**
 * Scans the header of the file searching for magic numbers and version bits
 * Only after that the header can be read in full
 */
public class NesHeaderScanner extends NesHeaderHandler {

    public record Result(
            MagicNumber magicNumber,
            NesHeader.Version version,
            List<Problem> problems
    ) {
    }

    public Result scan(UByteBuffer headerBuffer) {
        final List<Problem> problems = new ArrayList<>();
        final MagicNumber magicNumber = detectMagicNumber(problems, headerBuffer);
        final NesHeader.Version version = detectVersion(headerBuffer);

        headerBuffer.rewind();

        return new Result(magicNumber, version, problems);
    }

    // TODO: Go through all magic numbers and select the one with highest match percentage
    /* package */ MagicNumber detectMagicNumber(List<Problem> problems, UByteBuffer headerBuffer) {
        @Unsigned byte[] fourBytes = getMagic(headerBuffer);

        int matchPercent = MagicNumber.GAME_NES.matchesPartially(fourBytes);
        assert matchPercent >= 0 && matchPercent <= 100 : "wrap percentage in a record"; // TODO: do it
        if (0 <= matchPercent && matchPercent < 75) {
            problems.add(new Problem(MAJOR, "Less than 75% of magic number is matching: " + Hex.s(fourBytes)));
        } else if (75 <= matchPercent && matchPercent < 100) {
            problems.add(new Problem(MINOR, "More than 75% of magic number is matching: " + Hex.s(fourBytes)));
        }

        return MagicNumber.GAME_NES;
    }

    /* package */ NesHeader.Version detectVersion(UByteBuffer header) {
        int versionBits = getVersion(header);

        @Unsigned byte[] bytes12to15 = new byte[4];
        header.get(12, bytes12to15);

        if (versionBits == 0b10) { // TODO: & size taking into account byte 9 does not exceed the actual size of the ROM image
            return NesHeader.Version.NES_2_0;
        }

        if (versionBits == 0b00 && allZeros(bytes12to15)) {
            return NesHeader.Version.MODERN_iNES;
        }

        @Unsigned byte[] bytes7to15 = new byte[9];
        header.get(7, bytes7to15);
        String maybeDiskDude = new String(bytes7to15, StandardCharsets.US_ASCII);

        if (maybeDiskDude.equals("DiskDude!") || versionBits == 0b01) { // full string or just part of D
            // TODO: what about astra and other "vendors", maybe detect header trailing text in general
            return NesHeader.Version.ARCHAIC_iNES;
        }

        return NesHeader.Version.NES_0_7;
    }

    private boolean allZeros(@Unsigned byte[] bytes) {
        for (@Unsigned byte b : bytes) {
            if (uint(b) != 0) { return false; }
        }

        return true;
    }
}
