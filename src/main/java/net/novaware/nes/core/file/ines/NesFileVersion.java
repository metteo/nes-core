package net.novaware.nes.core.file.ines;

import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.novaware.nes.core.util.Asserts.assertArgument;

// TODO: convert to enum or make separate subclasses if switch is required
public sealed interface NesFileVersion
        extends Comparable<NesFileVersion>
        permits NesFileVersion.Impl
{
    NesFileVersion UNKNOWN     = of("Unknown",       "?.?.?",   -1, false, null);

    NesFileVersion ARCHAIC     = of("Archaic iNES",  "0.6.8",   68,  true, null);
    NesFileVersion ARCHAIC_0_7 = of("iNES 0.7",      "0.7.4",   74,  true, ARCHAIC);     // MapperHi

    NesFileVersion MODERN      = of("The iNES",      "1.7.5", 1075,  true, ARCHAIC_0_7); // VS.System
    NesFileVersion MODERN_1_3  = of("iNES 1.3",      "1.7.6", 1076, false, MODERN);      // PlayChoice-10 bit, Version (future)
    NesFileVersion MODERN_1_5  = of("iNES 1.5",      "1.9.1", 1091,  true, MODERN_1_3);  // PRG RAM size, TV System
    NesFileVersion MODERN_1_7  = of("iNES 1.7",     "1.10.4", 1104, false, MODERN_1_5);  // PRG RAM presence, TV System (multi), Bus Conflicts

    NesFileVersion FUTURE      = of("NES 2.0",      "2.15.6", 2156,  true, MODERN_1_3);  // Bytes 7-15 revisited

    /**
     * Name used in nesdev.org wiki. Variants beyond iNES are
     * specific to this code (not in common use)
     */
    String name();

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
    String textual();

    /**
     * Numeric variant which can be compared
     */
    int numeric();

    /**
     * False if the version is unofficial (the bits)
     */
    boolean official();

    /**
     * The previous version. iNES and NES 2.0 diverged at some point.
     */
    @Nullable NesFileVersion parent(); // TODO: fix the nullability

    private static NesFileVersion of(String name, String textual, int numeric, boolean official, @Nullable NesFileVersion parent) {
        return new Impl(name, textual, numeric, official, parent);
    }

    static List<NesFileVersion> values() {
        return List.of(ARCHAIC, ARCHAIC_0_7, MODERN, MODERN_1_3, MODERN_1_5, MODERN_1_7, FUTURE);
    }

    // TODO: implement method that follows the parent chain and returns versions in ascending order

    final class Impl implements NesFileVersion {

        private final String name;
        private final String textual;
        private final int numeric;
        private final boolean official;
        private final @Nullable NesFileVersion parent;

        Impl(
                String name,
                String textual,
                int numeric,
                boolean official,
                @Nullable NesFileVersion parent
        ) {
            this.name = name;
            this.textual = textual;
            this.numeric = numeric;
            this.official = official;
            this.parent = parent;
        }

        @Override public String name() { return name; }
        @Override public String textual() { return textual; }
        @Override public int numeric() { return numeric; }
        @Override public boolean official() { return official; }
        @Override public @Nullable NesFileVersion parent() { return parent; }

        // TODO: hashCode & equals
        // TODO: ordinal
        // TODO:

        @Override
        public int compareTo(NesFileVersion that) {
            assertArgument(that != null, "that must not be null");

            return Integer.compare(this.numeric, that.numeric());
        }
    }
}
