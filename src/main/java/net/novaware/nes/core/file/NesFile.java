package net.novaware.nes.core.file;

import net.novaware.nes.core.util.Quantity;

import java.nio.ByteBuffer;

// TODO: convert to an auto value?
public class NesFile {

    // region Source

    /**
     * Location the file was loaded from (either disk or url)
     */
    private String origin;

    // endregion
    // region Memory Bus / Control Logic

    /**
     * Number of the mapper
     */
    private short mapper;

    /**
     * Board bus conflict status
     */
    private boolean busConflicts;

    // endregion
    // region CPU

    /**
     * Additional memory for the CPU to use
     * Also PRG-RAM / Work RAM / WRAM
     */
    private ProgramMemory programMemory;

    /**
     * Instructions for the CPU to execute
     * <br>
     * PRG-ROM / Program ROM
     */
    private ByteBuffer programData;

    // endregion
    // region PPU

    /**
     * Amount of memory CPU fill for the PPU
     * CHR-RAM / Video RAM / VRAM
     */
    private Quantity videoMemory;

    /**
     * Graphics data for the PPU to render
     * CHR-ROM / Character ROM
     */
    private ByteBuffer videoData;

    /**
     * Determines the speed of CPU and color space
     */
    private VideoStandard videoStandard;

    /**
     * Specifies the structure of video data
     */
    private Mirroring mirroring;

    // endregion
    // region Other

    /**
     * Optional original header read from iNES / NES 2.0 file
     */
    private ByteBuffer legacyHeader; // TODO: add method for printing bytes in binary and as string (diskdude!)

    /**
     * Usually contains mapper register translation and video memory caching code
     */
    private ByteBuffer trainerData;

    /**
     * Remainder of data after all specified sections in the file
     */
    private ByteBuffer remainingData;

    // endregion

    public NesFile(
            String origin,

            short mapper,
            boolean busConflicts,

            ProgramMemory programMemory,
            ByteBuffer programData,

            Quantity videoMemory,
            ByteBuffer videoData,
            VideoStandard videoStandard,
            Mirroring mirroring,

            ByteBuffer legacyHeader,
            ByteBuffer trainerData,
            ByteBuffer remainingData
    ) {
        this.origin = origin;

        this.mapper = mapper;
        this.busConflicts = busConflicts;

        this.programMemory = programMemory;
        this.programData = programData;

        this.videoMemory = videoMemory;
        this.videoData = videoData;
        this.videoStandard = videoStandard;
        this.mirroring = mirroring;

        this.legacyHeader = legacyHeader;
        this.trainerData = trainerData;
        this.remainingData = remainingData;
    }

    public String getOrigin() {
        return origin;
    }

    public short getMapper() {
        return mapper;
    }

    public boolean hasBusConflicts() {
        return busConflicts;
    }

    public boolean hasProgramMemory() {
        return programMemory != null && programMemory.size.amount() > 0;
    }

    public ProgramMemory getProgramMemory() {
        return programMemory;
    }

    public ByteBuffer getProgramData() {
        return programData;
    }

    public boolean hasVideoMemory() {
        return videoMemory != null && videoMemory.amount() > 0;
    }

    public Quantity getVideoMemory() {
        return videoMemory;
    }

    public boolean hasVideoData() {
        return videoData != null && videoData.capacity() > 0;
    }

    public ByteBuffer getVideoData() {
        return videoData;
    }

    public VideoStandard getVideoStandard() {
        return videoStandard;
    }

    public Mirroring getMirroring() {
        return mirroring;
    }

    public boolean hasLegacyHeader() {
        return legacyHeader != null && legacyHeader.capacity() > 0;
    }

    public ByteBuffer getLegacyHeader() {
        return legacyHeader;
    }

    public boolean hasTrainer() {
        return trainerData != null && trainerData.capacity() > 0;
    }

    public ByteBuffer getTrainerData() {
        return trainerData;
    }

    public ByteBuffer getRemainingData() {
        return remainingData;
    }

    public enum Mirroring {
        VERTICAL, // 0
        HORIZONTAL, // 1
    }

    public record ProgramMemory(Kind kind, Quantity size) {

    }

    public enum Kind {
        NONE,
        VOLATILE, // no battery
        PERSISTENT // battery
    }

    public enum VideoStandard {
        NTSC, NTSC_HYBRID, PAL, PAL_HYBRID, OTHER
    }


}


