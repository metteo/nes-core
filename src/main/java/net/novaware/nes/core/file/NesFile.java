package net.novaware.nes.core.file;

import com.google.auto.value.AutoBuilder;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.Quantity.Unit;

import java.nio.ByteBuffer;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

public record NesFile (
    String origin,
    Meta meta,
    // TODO: maybe and checksums / hash section so it's possible to lookup info in xml header db or online
    Data data
) {

    /**
     * Metadata section usually read from NES ROM file.
     *
     * @param title of the game possibly found at the end of the file (127-128 bytes),
     *              defaults to the file name without extension if not available
     * @param info trailing section of the header if it contains ascii text (e.g. "DiskDude!")
     *
     * @param system specifies if the file is for regular NES or arcade variants
     *
     * @param mapper number
     * @param busConflicts of the board
     *
     * @param programMemory additional memory for the CPU to use
     *                      (also PRG-RAM / Work RAM / WRAM or Save RAM / SRAM)
     * @param programData instructions for the CPU to execute (also PRG-ROM / Program ROM)
     *
     * @param videoMemory amount of memory CPU can fill for the PPU (also CHR-RAM / Video RAM / VRAM)
     * @param videoData graphics data for the PPU to render (also CHR-ROM / Character ROM)
     * @param videoStandard determines the speed of CPU and color space
     * @param layout specifies the arrangement of video data
     */

    public record Meta (
        // FIXME: create constructor validating all the values are not null! Objects.requireNonNull to the rescue!
        String title,
        String info,

        System system,

        short mapper,
        boolean busConflicts,

        Quantity trainer, // FIXME: should be trainerData? or trainerMemory

        ProgramMemory programMemory,
        Quantity programData,

        Quantity videoMemory, // TODO: what size in iNES
        Quantity videoData,
        VideoStandard videoStandard,
        Layout layout,

        Quantity remainder
    ) {

        public static Builder builder() {
            return new AutoBuilder_NesFile_Meta_Builder();
        }

        public static Builder builder(Meta meta) {
            return new AutoBuilder_NesFile_Meta_Builder(meta);
        }

        @AutoBuilder
        public interface Builder {
            Builder title(String title);
            Builder info(String info);

            Builder system(System system);

            Builder mapper(short mapper);
            default
            Builder mapper(int mapper) { return mapper((short) mapper); }
            Builder busConflicts(boolean busConflicts);

            Builder trainer(Quantity trainer);

            Builder programMemory(ProgramMemory programMemory);
            Builder programData(Quantity programData);
            Builder videoMemory(Quantity videoMemory);
            Builder videoData(Quantity videoData);
            Builder videoStandard(VideoStandard videoStandard);
            Builder layout(Layout layout);

            Builder remainder(Quantity remainder);

            default Builder noTitle() { return title(""); }
            default Builder noInfo() { return info(""); }
            default Builder noTrainer() { return trainer(new Quantity(0, Unit.BANK_512B)); }
            default Builder noProgramMemory() {
                return programMemory(new ProgramMemory(Kind.NONE, new Quantity(0, Unit.BANK_8KB)));
            }
            default Builder noProgramData() { return programData(new Quantity(0, Unit.BANK_16KB)); }
            default Builder noVideoMemory() { return videoMemory(new Quantity(0, Unit.BANK_8KB)); }
            default Builder noVideoData() { return videoData(new Quantity(0, Unit.BANK_8KB)); }
            default Builder noVideoStandard() { return videoStandard(VideoStandard.UNKNOWN); }
            default Builder noRemainder() { return remainder(new Quantity(0, Unit.BYTES)); }

            Meta build();
        }
    }

    /**
     * NES File split into distinct data sections
     *
     * @param header optional original header read from iNES / NES 2.0 file
     * @param trainer usually contains mapper register translation and video memory caching code
     * @param program instructions for the CPU to execute (PRG-ROM / Program ROM)
     * @param video graphics data for the PPU to render (CHR-ROM / Character ROM)
     * @param inst Playchoice-10 INSTruction data displayed on the second screen (8KB)
     * @param prom Playchoice-10 ProgrammableROM sections for decryption of inst (2x16B) (RP5H01)
     * @param remainder data after all specified sections in the file. May contain game title
     */
    public record Data (
        ByteBuffer header,
        ByteBuffer trainer,
        ByteBuffer program,
        ByteBuffer video,
        ByteBuffer inst,
        ByteBuffer prom,
        ByteBuffer remainder
    ) {

        private static boolean hasData(ByteBuffer buffer) {
            return buffer.capacity() > 0;
        }

        public boolean hasHeader() {
            return hasData(header);
        }

        public boolean hasTrainer() {
            return hasData(trainer);
        }

        public boolean hasVideo() {
            return hasData(video);
        }

        public boolean hasRemainder() {
            return hasData(remainder);
        }

        public static Data.Builder builder() {
            return new AutoBuilder_NesFile_Data_Builder();
        }

        public static Data.Builder builder(Data data) {
            return new AutoBuilder_NesFile_Data_Builder(data);
        }

        @AutoBuilder
        public interface Builder {
            Builder header(ByteBuffer header);

            Builder trainer(ByteBuffer trainer);
            Builder program(ByteBuffer program);
            Builder video(ByteBuffer video);
            Builder inst(ByteBuffer inst);
            Builder prom(ByteBuffer prom);
            Builder remainder(ByteBuffer remainder);

            private static ByteBuffer emptyBuffer() { return ByteBuffer.allocate(0); }

            default Builder noHeader() { return header(emptyBuffer()); }
            default Builder noTrainer() { return trainer(emptyBuffer()); }
            default Builder noProgram() { return program(emptyBuffer()); }
            default Builder noVideo() { return video(emptyBuffer()); }
            default Builder noInst() { return inst(emptyBuffer()); }
            default Builder noProm() { return prom(emptyBuffer()); }
            default Builder noRemainder() { return remainder(emptyBuffer()); }

            Data build();
        }
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
        private final byte bits;

        @SuppressWarnings("unused")
        Layout(Mirroring mirroring, int bits) {
            this.mirroring = mirroring;
            this.bits = ubyte(bits);
        }

        public Mirroring mirroring() {
            return mirroring;
        }

        public int bits() {
            return uint(bits) & BITS_MASK;
        }
    }

    public enum Mirroring {
        HORIZONTAL,
        VERTICAL,

        UNKNOWN
    }

    public record ProgramMemory(Kind kind, Quantity size) {
        // TODO: invariants
    }

    public enum Kind {
        NONE,
        VOLATILE, // no battery
        PERSISTENT, // battery
        UNKNOWN
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
        VS_SYSTEM("VS.System",          0b01),
        PLAY_CHOICE_10("PlayChoice-10", 0b10),
        EXTENDED("Extended Console",    0b11); // TODO: add more enum values

        public static final int BITS_MASK           = 0b1001;

        private final String displayName;
        private final byte bits;

        System(String displayName, int bits) {
            this.displayName = displayName;
            this.bits = ubyte(bits);
        }

        public String displayName() {
            return displayName;
        }

        public int bits() {
            return uint(bits) & BITS_MASK;
        }
    }

    public enum VideoStandard {
        NTSC,
        NTSC_HYBRID,
        PAL,
        PAL_HYBRID,
        DENDY,
        OTHER,
        UNKNOWN
    }
}


