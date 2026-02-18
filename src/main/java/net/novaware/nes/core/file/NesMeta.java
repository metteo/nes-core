package net.novaware.nes.core.file;

import com.google.auto.value.AutoBuilder;
import net.novaware.nes.core.util.Quantity;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.sint;

/**
 * Metadata section usually read from NES ROM file or from ROM Meta DB.
 *
 * @param title         of the game possibly found at the end of the file (127-128 bytes),
 *                      defaults to the file name without extension if not available
 * @param info          trailing section of the header if it contains ascii text (e.g. "DiskDude!").
 *                      May contain name of the software that generated / processed the file
 *
 * @param system        specifies if the file is for regular NES or other variants / derivatives
 *
 * @param mapper        number
 * @param busConflicts  of the board

 *
 * @param programMemory additional memory for the CPU to use
 *                      (also PRG-RAM / Work RAM / WRAM or Save RAM / SRAM)
 * @param trainer       size / presence, usually 0 / 512 bytes of trainer (emulator / cartridge copier specific code)
 *                      copied into Program Memory address space
 * @param programData   instructions for the CPU to execute (also PRG-ROM / Program ROM)
 *
 * @param videoMemory   amount of memory CPU can fill for the PPU (also CHR-RAM / Video RAM / VRAM)
 * @param videoData     graphics data for the PPU to render (also CHR-ROM / Character ROM): layout and size
 * @param videoStandard determines the speed of CPU and color space
 *
 * @param footer        size of the footer, kept for generation / write back purposes
 */
public record NesMeta(
        // FIXME: create constructor validating all the values are not null! Objects.requireNonNull to the rescue!
        String title,
        String info,

        System system,

        @Signed short mapper, // TODO: switch to int
        // byte subMapper, // TODO: uncomment when adding NES 2.0 parsing
        boolean busConflicts,

        ProgramMemory programMemory,
        Quantity trainer,
        Quantity programData,

        Quantity videoMemory, // TODO: what size in iNES
        VideoData videoData,
        VideoStandard videoStandard,

        Quantity footer
) {

    public static Builder builder() {
        return new AutoBuilder_NesMeta_Builder();
    }

    public static Builder builder(NesMeta meta) {
        return new AutoBuilder_NesMeta_Builder(meta);
    }

    @AutoBuilder
    public interface Builder {
        Builder title(String title);

        Builder info(String info);

        Builder system(System system);

        Builder mapper(short mapper);

        default Builder mapper(int mapper) {
            return mapper((short) mapper);
        }

        Builder busConflicts(boolean busConflicts);

        Builder programMemory(ProgramMemory programMemory);

        Builder trainer(Quantity trainer);

        Builder programData(Quantity programData);

        Builder videoMemory(Quantity videoMemory);

        Builder videoData(VideoData videoData);

        Builder videoStandard(VideoStandard videoStandard);

        Builder footer(Quantity footer);

        default Builder noTitle() {
            return title("");
        }

        default Builder noInfo() {
            return info("");
        }

        default Builder noProgramMemory() {
            return programMemory(new ProgramMemory(Kind.NONE, new Quantity(0, Quantity.Unit.BANK_8KB)));
        }

        default Builder noTrainer() {
            return trainer(new Quantity(0, Quantity.Unit.BANK_512B));
        }

        default Builder noProgramData() {
            return programData(new Quantity(0, Quantity.Unit.BANK_16KB));
        }

        default Builder noVideoMemory() {
            return videoMemory(new Quantity(0, Quantity.Unit.BANK_8KB));
        }

        default Builder noVideoData() {
            return videoData(new VideoData(Layout.UNKNOWN, new Quantity(0, Quantity.Unit.BANK_8KB)));
        }

        default Builder noVideoStandard() {
            return videoStandard(VideoStandard.UNKNOWN);
        }

        default Builder noFooter() {
            return footer(new Quantity(0, Quantity.Unit.BYTES));
        }

        NesMeta build();
    }

    /**
     * Nametable / Video Memory ... arrangement / mirroring / layout
     * may map into single or four screen depending on the mapper
     *
     * <a href="https://www.nesdev.org/wiki/NES_2.0#Nametable_layout">Nametable layout on nesdev.org</a>
     */
    public enum Layout { // FIXME: ScreenLayout? like V, H, 4, 1, D, L, 3V, 3H, 1F
        STANDARD_VERTICAL      (Mirroring.HORIZONTAL, 0b0000),
        STANDARD_HORIZONTAL    (Mirroring.VERTICAL,   0b0001),
        ALTERNATIVE_VERTICAL   (Mirroring.HORIZONTAL, 0b1000),
        ALTERNATIVE_HORIZONTAL (Mirroring.VERTICAL,   0b1001),

        UNKNOWN                (Mirroring.UNKNOWN,    0b0000);

        public static final int BITS_MASK           = 0b1001;

        private final Mirroring mirroring;
        private final @Unsigned byte bits;

        @SuppressWarnings("unused")
        Layout(Mirroring mirroring, int bits) {
            this.mirroring = mirroring;
            this.bits = ubyte(bits);
        }

        public static Layout fromBits(int bits) {
            for (Layout layout : values()) {
                if (sint(layout.bits) == (bits & BITS_MASK)) {
                    return layout;
                }
            }

            return UNKNOWN;
        }

        public Mirroring mirroring() {
            return mirroring;
        }

        public int bits() {
            return sint(bits) & BITS_MASK;
        }
    }

    public enum Mirroring {
        HORIZONTAL,
        VERTICAL,

        UNKNOWN
    }

    public enum Kind {
        NONE,       // no work ram, open bus
        VOLATILE,   // no battery, work ram
        PERSISTENT, // battery, save ram
        UNKNOWN     // memory absence bit not set
    }

    /**
     * Enumeration of systems supported by iNES / NES 2.0 spec
     *
     * @see <a href="https://www.mariowiki.com/Nintendo_Entertainment_System">Nintendo Entertainment System</a>
     * @see <a href="https://www.mariowiki.com/VS._System">VS.System</a>
     * @see <a href="https://www.mariowiki.com/Nintendo_PlayChoice-10">Nintendo PlayChoice-10</a>
     * @see <a href="https://www.nesdev.org/wiki/NES_2.0#Extended_Console_Type">Extended Console</a>
     */
    public enum System { // FIXME: rename, clashes with java.lang.System

        NES("NES",                      0b00),
        VS_SYSTEM("VS.System",          0b01), // Vs. games have a coin slot and different palettes.
        PLAY_CHOICE_10("PlayChoice-10", 0b10), // 8 KB of Hint Screen data stored after CHR data
        EXTENDED("Extended Console",    0b11), // TODO: add more enum values

        UNKNOWN("Unknown",              0xFF); // TODO: what to do with you?

        public static final int BITS_MASK = 0b11;

        private final String displayName;
        private final @Unsigned byte bits;

        System(String displayName, int bits) {
            this.displayName = displayName;
            this.bits = ubyte(bits);
        }

        public String displayName() {
            return displayName;
        }

        public int bits() {
            return sint(bits) & BITS_MASK;
        }

        public static System fromBits(int bits) { // NOTE: modern nes only, 2.0 will need extension
            for (System system : values()) {
                if (sint(system.bits) == (bits & BITS_MASK)) {
                    return system;
                }
            }

            return UNKNOWN;
        }
    }

    /**
     * {@link VideoStandard#NTSC}  Ratio: 3.0 | Hz: 60 <br>
     * {@link VideoStandard#PAL}   Ratio: 3.2 | Hz: 50 <br>
     * {@link VideoStandard#DENDY} Ratio: 3.0 | Hz: 50
     */
    public enum VideoStandard {
                     //      iNES        | NES 2.0
                     // byte 9 | byte 10 | byte 12
        NTSC,        // 0b0    | 0b00    | 0b00
        NTSC_DUAL,   // 0b0    | 0b01    | 0b10
        PAL,         // 0b1    | 0b10    | 0b01
        PAL_DUAL,    // 0b1    | 0b11    | -
        DENDY,       // -      | -       | 0b11

        UNKNOWN      // Archaic iNES / iNES 0.7
    }

    public record ProgramMemory(Kind kind, Quantity size) {
        // TODO: invariants
    }

    public record VideoData(Layout layout, Quantity size) {
        // TODO: invariants
    }
}
