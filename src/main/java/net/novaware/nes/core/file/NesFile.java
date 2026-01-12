package net.novaware.nes.core.file;

import com.google.auto.value.AutoBuilder;
import net.novaware.nes.core.util.Quantity;
import net.novaware.nes.core.util.Quantity.Unit;

import java.nio.ByteBuffer;
import java.util.Map;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.uint;

/**
 * There is some intended duplication in this record:
 * <ul>
 *     <li>
 *         <code>meta</code> & <code>data.header</code> fields -
 *         When reading a file header field is populated with an
 *         original version of the header if available and then
 *         parsed into meta section.
 *         Later if the whole file needs to be saved the original
 *         header will be used instead of parsed section to
 *         preserve it.
 *         It's possible original header when read was in old
 *         version and user may want to upgrade it.
 *         In such case a converter will use the meta section to
 *         create new header and replace the old one in data section.
 *         It's possible there is no header (.unh file). In such
 *         case it's necessary to get meta info from external
 *         source (xml, online) and possibly generate a header.
 *     </li>
 *     <li>
 *         <code>meta.title</code> & <code>data.remainder</code> -
 *         When reading a file trailing section may contain the
 *         title of the software. Such information is put in
 *         title field but only the ASCII printable part.
 *         The original trailing data is stored in data.remainder
 *         If there is no trailing data the title is derived from
 *         the origin part (file name without the extension)
 *         Later if the whole file needs to be saved the original
 *         footer will be used instead of parsed section to
 *         preserve it.
 *         The trailing data in the file is non-standard and
 *         user may want to clear it out or on the other hand
 *         add it based on the file name or other information.
 *     </li>
 *     <li>
 *         sizes in meta and sizes of buffers in data sections -
 *         the process of reading the files is executed in stages
 *         First, the fixed size header is read and parsed.
 *         This gives the information about the rest of data.
 *         Different slicing points are calculated and added
 *         in meta for later use.
 *         Second, different sections of the file are sliced
 *         into dedicated buffers and stored in data.
 *         Third, hash values are calculated and stored in
 *         hash section.
 *
 *         In case of headerless file:
 *         First hash whole file and look up meta info about it
 *         Second, slice out sections of the file
 *         Third, hash data sections
 *         Fourth, verify integrity against looked up info.
 *
 *         In case of generated file meta section acts as a
 *         blueprint how random data section should be
 *         generated. Hash section is calculated afterward.
 *     </li>
 * </ul>
 * @param origin location of the file (file system or online)
 * @param meta information required to interpret the data
 * @param data different parts of a NES software
 * @param hash results used to identify the software
 */
public record NesFile (
    String origin,
    Meta meta,
    Data data,
    Hash hash
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
     * @param trainer size / presence, usually 0 / 512 bytes
     *
     * @param programMemory additional memory for the CPU to use
     *                      (also PRG-RAM / Work RAM / WRAM or Save RAM / SRAM)
     * @param programData instructions for the CPU to execute (also PRG-ROM / Program ROM)
     *
     * @param videoMemory amount of memory CPU can fill for the PPU (also CHR-RAM / Video RAM / VRAM)
     * @param videoData graphics data for the PPU to render (also CHR-ROM / Character ROM)
     * @param videoStandard determines the speed of CPU and color space
     * @param layout specifies the arrangement of video data
     *
     * @param remainder size of the footer? probably kept for generation / write back purposes
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
        ByteBuffer inst, // TODO: consider play choice subsection for inst and prom
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
            Builder remainder(ByteBuffer remainder); // TODO: rename to footer

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
     * Sections of Data passed through MessageDigest / hashing functions
     * Useful for metadata lookup in XML header DB / online or data verification
     *
     * @param file of a whole file (if unheadered) or some relevant sections
     *             like program and video (if headered)
     * @param trainer hash
     * @param program hash
     * @param video hash
     * @param inst hash
     * @param prom hash
     */
    public record Hash (
        Map<HashAlgorithm, String> file,

        Map<HashAlgorithm, String> trainer,
        Map<HashAlgorithm, String> program,
        Map<HashAlgorithm, String> video,
        Map<HashAlgorithm, String> inst,
        Map<HashAlgorithm, String> prom
    ) {

        public static Hash empty() {
            return new Hash(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }
    }

    /**
     * Common message digest / integrity algorithms used to identify NES files
     *
     */
    public enum HashAlgorithm {
        SUM16, // sum of all bytes modulo 16 in hex
        CRC32, // in hex
        MD5,
        SHA1,
        SHA256
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


