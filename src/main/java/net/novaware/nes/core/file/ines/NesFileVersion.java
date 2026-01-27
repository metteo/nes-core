package net.novaware.nes.core.file.ines;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.reverse;

public enum NesFileVersion implements Comparable<NesFileVersion> {
//              displayName,     textual,  num,  off., major, year, parent
    UNKNOWN    ("Unknown",       "?.?.?",   -1, false, false,   -1, null       ),

    ARCHAIC    ("Archaic iNES",  "0.6.8",   68,  true,  true, 1996, null       ),
    ARCHAIC_0_7("iNES 0.7",      "0.7.4",   74,  true, false, 1997, ARCHAIC    ), // MapperHi

    MODERN     ("The iNES",      "1.7.5", 1075,  true,  true, 1998, ARCHAIC_0_7), // VS.System
    MODERN_1_3 ("iNES 1.3",      "1.7.6", 1076, false, false, 2000, MODERN     ), // PlayChoice-10 bit, Version (future)
    MODERN_1_5 ("iNES 1.5",      "1.9.1", 1091,  true, false, 2003, MODERN_1_3 ), // PRG RAM size, TV System
    MODERN_1_7 ("iNES 1.7",     "1.10.4", 1104, false, false, 2005, MODERN_1_5 ), // PRG RAM presence, TV System (multi), Bus Conflicts

    FUTURE     ("NES 2.0",      "2.15.6", 2156,  true,  true, 2006, MODERN     ); // Bytes 7-15 revisited

    /**
     * Name used in nesdev.org wiki. Variants beyond iNES are
     * specific to this code (not in common use)
     */
    private final String displayName;

    /**
     * Textual version similar to semantic versioning
     * <p> major, minor, patch
     * <p> era  , byte,  bit
     * <p>
     * where:
     * <ul>
     * <li> era: 0 - archaic, 1 - modern, 2 - future
     * <li> byte: up to which header byte was used
     * <li> bit: up to how many bits of the byte were used
     * <p>
     * 1.7.6 means: modern header with byte 7 used up to 6 bits
     */
    private final String textual;

    /**
     * Numeric variant which can be compared
     */
    private final int numeric;

    /**
     * False if the version is unofficial (the bits)
     */
    private final boolean official;

    private final boolean major;

    /**
     * Estimated year of release (@Gemieni, I'm looking at you :))
     */
    private final int releaseYear;

    /**
     * The previous version. iNES and NES 2.0 diverged at some point.
     */
    private final @Nullable NesFileVersion parent;

    private static final List<NesFileVersion> VALUE_LIST;

    static {
        List<NesFileVersion> values = new ArrayList<>(List.of(values()));
        values.remove(UNKNOWN);

        VALUE_LIST = List.copyOf(values);
    }
    public static final Comparator<NesFileVersion> COMPARATOR_NUMERIC = Comparator.comparing(NesFileVersion::numeric);

    public static List<NesFileVersion> valueList() {
        return VALUE_LIST;
    }

    NesFileVersion(
            String displayName,
            String textual,
            int numeric,
            boolean official,
            boolean major,
            int releaseYear,
            @Nullable NesFileVersion parent
    ) {
        this.displayName = displayName;
        this.textual = textual;
        this.numeric = numeric;
        this.official = official;
        this.major = major;
        this.releaseYear = releaseYear;
        this.parent = parent;
    }

    public String displayName() { return displayName; }
    public String textual() { return textual; }
    public int numeric() { return numeric; }
    public boolean isOfficial() { return official; }
    public boolean isMajor() { return major; }
    public int releaseYear() { return releaseYear; }
    public @Nullable NesFileVersion parent() { return parent; }

    public List<NesFileVersion> getHistory() {
        List<NesFileVersion> history = new ArrayList<>();

        NesFileVersion current = this;
        while (current != null && !history.contains(current)) { // no infinite loop if parent==this by mistake
            history.add(current);
            current = current.parent;
        }

        reverse(history);
        return history;
    }
}
