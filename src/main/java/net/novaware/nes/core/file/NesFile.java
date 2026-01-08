package net.novaware.nes.core.file;

import com.google.auto.value.AutoBuilder;
import net.novaware.nes.core.util.Quantity;

import java.nio.ByteBuffer;

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
     * @param programMemory additional memory for the CPU to use (also PRG-RAM / Work RAM / WRAM)
     * @param programData instructions for the CPU to execute (also PRG-ROM / Program ROM)
     *
     * @param videoMemory amount of memory CPU can fill for the PPU (also CHR-RAM / Video RAM / VRAM)
     * @param videoData graphics data for the PPU to render (also CHR-ROM / Character ROM)
     * @param videoStandard determines the speed of CPU and color space
     * @param mirroring specifies the arrangement of video data
     */

    public record Meta (
        String title,
        String info,

        System system,

        short mapper,
        boolean busConflicts,

        Quantity trainer,

        ProgramMemory programMemory,
        Quantity programData,

        Quantity videoMemory, // TODO: what size in iNES
        Quantity videoData,
        VideoStandard videoStandard,
        Mirroring mirroring,

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
            Builder busConflicts(boolean busConflicts);

            Builder trainer(Quantity trainer);

            Builder programMemory(ProgramMemory programMemory);
            Builder programData(Quantity programData);
            Builder videoMemory(Quantity videoMemory);
            Builder videoData(Quantity videoData);
            Builder videoStandard(VideoStandard videoStandard);
            Builder mirroring(Mirroring mirroring);

            Builder remainder(Quantity remainder);

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
            return buffer != null && buffer.capacity() > 0;
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
    }

    public enum Mirroring {
        VERTICAL, // 0
        HORIZONTAL, // 1
        SINGLE_SCREEN,
        FOUR_SCREEN
    }

    public record ProgramMemory(Kind kind, Quantity size) {
    }

    public enum Kind {
        NONE,
        UNKNOWN,
        VOLATILE, // no battery
        PERSISTENT // battery
    }

    public enum System {

        /**
         * <a href="https://www.mariowiki.com/Nintendo_Entertainment_System">Nintendo Entertainment System</a>
         */
        NES("Nintendo Entertainment System", false),

        /**
         * <a href="https://www.mariowiki.com/VS._System">VS.System</a>
         */
        VS_SYSTEM("VS.System", true),

        /**
         * <a href="https://www.mariowiki.com/Nintendo_PlayChoice-10">Nintendo PlayChoice-10</a>
         */
        PLAY_CHOICE_10("Nintendo PlayChoice-10", true);

        private String name;
        private boolean arcade;

        System(String name, boolean arcade) {
            this.name = name;
            this.arcade = arcade;
        }
    }

    public enum VideoStandard {
        NTSC, NTSC_HYBRID, PAL, PAL_HYBRID, DENDY, OTHER
    }
}


