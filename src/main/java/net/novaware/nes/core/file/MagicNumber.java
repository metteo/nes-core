package net.novaware.nes.core.file;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.novaware.nes.core.util.UnsignedTypes.sint;

public enum MagicNumber {

    // 0x1A is a SUB character, Ctrl+Z, MS-DOS EOF
    GAME_NES ("NES\u001a"), // iNES / NES 2.0
    GAME_FDS ("FDS\u001a"), // Famicom Disk System
    GAME_UNIF("UNIF"), // Universal NES Image Format

    PATCH_IPS("PATCH"),
    PATCH_BPS("BPS1"),
    PATCH_UPS("UPS1"),

    MUSIC_NSF_CLASSIC ("NESM\u001a\u0001"), // NES Sound Format 1 (Classic)
    MUSIC_NSF_MODERN  ("NESM\u001a\u0002"), // NES Sound Format 2 (Modern)
    MUSIC_NSF_EXTENDED("NSFe"); // NES Sound Format (Extended Metadata)

    private final String symbols;

    MagicNumber(String symbols) {
        this.symbols = symbols;
    }

    public @Unsigned byte[] numbers() {
        return symbols.getBytes(US_ASCII);
    }

    /**
     *
     * @param bytes to match
     * @return percent of bytes that match (0-100%)
     */
    public int matchesPartially(@Unsigned byte[] bytes) {
        @Unsigned byte[] magicBytes = numbers();
        int matches = 0;
        for (int i = 0; i < Math.min(bytes.length, magicBytes.length); i++) {
            if (sint(bytes[i]) == sint(magicBytes[i])) {
                matches++;
            }
        }

        return (int) ((double) matches / magicBytes.length * 100);
    }

    public boolean matches(@Unsigned byte[] bytes) {
        return matchesPartially(bytes) == 100;
    }
}
