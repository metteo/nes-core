package net.novaware.nes.core.config;

import java.util.List;

/**
 * @see <a href="https://www.nesdev.org/wiki/Cycle_reference_chart">Cycle reference chart on nesdev.org</a>
 */
public enum VideoStandard { // TODO: include post render scanline (241, NTSC black, PAL backdrop) and border region

    NTSC      (315 * 1_000_000d / 88 * 6, 12, 4, 262, true ),
    NTSC_DUAL (315 * 1_000_000d / 88 * 6, 12, 4, 262, true ),
    RGB       (315 * 1_000_000d / 88 * 6, 12, 4, 262, false),
    PAL       (4_433_618.75 * 6,          16, 5, 312, false),
    PAL_DUAL  (4_433_618.75 * 6,          16, 5, 312, false),
    DENDY     (4_433_618.75 * 6,          15, 5, 312, false), // PAL clock, unique CPU div
    PAL_M     (3_575_611d   * 6,          12, 4, 262, true ), // unique clock, NTSC divs

    UNKNOWN   (-1L, -1, -1, -1, false);

    public static final int PHYSICAL_WIDTH = 341; // horizontal, dots per scan line

    public static final int ACTIVE_WIDTH = 256; // horizontal dots  // TODO: maybe use unit checker here
    public static final int ACTIVE_HEIGHT = 240; // vertical dots

    public static final int V_BLANK_START = ACTIVE_HEIGHT + 1; // scan line
    public static final int H_BLANK_START = ACTIVE_WIDTH + 1; // dot / cycle

    // TODO: consider moving these into own enums: Clock, CpuModel, PpuModel etc
    private final double masterClock; // Hz // TODO: maybe use unit checker here
    private final int cpuDivisor;
    private final int ppuDivisor;
    private final int physicalHeight; // vertical, scan lines per frame
    private final boolean oddFrameCycleSkip;

    private static final List<VideoStandard> instances = List.of(values());

    VideoStandard(double masterClock, int cpuDiv, int ppuDiv, int physicalHeight, boolean oddFrameCycleSkip) {
        this.masterClock = masterClock;
        this.cpuDivisor = cpuDiv;
        this.ppuDivisor = ppuDiv;
        this.physicalHeight = physicalHeight;
        this.oddFrameCycleSkip = oddFrameCycleSkip;
    }

    public double getMasterClock() { // Hz
        return masterClock;
    }

    public int getPpuDivisor() {
        return ppuDivisor;
    }

    public int getCpuDivisor() {
        return cpuDivisor;
    }

    public int getApuDivisor() {
        return cpuDivisor * 2;
    }

    public int getDmaDivisor() {
        return cpuDivisor;
    }

    public double getCpuFrequency() {
        return masterClock / cpuDivisor;
    }

    public double getPpuFrequency() {
        return masterClock / ppuDivisor;
    }

    public boolean isOddFrameCycleSkip() {
        return oddFrameCycleSkip;
    }

    public double getRefreshRate() {
        return getPpuFrequency() / getPpuCyclesPerFrame();
    }

    public double getMasterCycles() {
        return masterClock / getRefreshRate();
    }

    public int getPpuCyclesPerFrame() {
        return physicalHeight * PHYSICAL_WIDTH;
    }

    public double getCpuCyclesPerFrame() {
        return getCpuFrequency() / getRefreshRate();
    }

    public int getPhysicalHeight() {
        return physicalHeight;
    }

    public int getPhysicalWidth() {
        return PHYSICAL_WIDTH;
    }

    public int getActiveWidth() {
        return ACTIVE_WIDTH;
    }

    public int getActiveHeight() {
        return ACTIVE_HEIGHT;
    }

    /**
     * TODO: this is nmi trigger line, 240 is already post render
     */
    public int getVerticalBlankStart() {
        return V_BLANK_START;
    }

    public int getHorizontalBlankStart() {
        return H_BLANK_START;
    }

    public static List<VideoStandard> instances() {
        return instances;
    }

    /**
     * Utility to print video standards to console
     */
    static void main() {
        instances().stream()
                .filter(vs -> vs != UNKNOWN)
                .map(VideoStandard::toText)
                .forEach(s -> System.out.println(s)); // NOTE: lambda will cause checker "Incompatible receiver type"
    }

    private static String toText(VideoStandard videoStandard) {
        return String.format("%-10s: FPS: %.3f Hz, CPU: %s, %s, PPU: %s, %s, Master: %s, %s",
                videoStandard.name(),
                videoStandard.getRefreshRate(),
                toMegaHertz(videoStandard.getCpuFrequency()),
                toCycles(videoStandard.getCpuCyclesPerFrame()),
                toMegaHertz(videoStandard.getPpuFrequency()),
                toCycles(videoStandard.getPpuCyclesPerFrame()),
                toMegaHertz(videoStandard.getMasterClock()),
                toCycles(videoStandard.getMasterCycles())
        );
    }

    private static String toMegaHertz(double hz) {
        return String.format("%.3f MHz", hz / 1_000_000d);
    }

    private static String toCycles(double cycles) {
        return String.format("%.1f cyc", cycles);
    }
}
