package net.novaware.nes.core.config;

public enum VideoStandard {

    NTSC      (21_477_272L, 12, 4, 262),
    NTSC_DUAL (21_477_272L, 12, 4, 262),
    PAL       (26_601_712L, 16, 5, 312),
    PAL_DUAL  (26_601_712L, 16, 5, 312),
    DENDY     (26_601_712L, 15, 5, 312), // PAL clock, unique CPU div
    PAL_M     (21_453_671L, 12, 4, 262), // unique clock, NTSC divs
    UNKNOWN   (        -1L, -1,-1,  -1);

    public static final int PHYSICAL_WIDTH = 341; // horizontal, dots per scan line

    public static final int ACTIVE_WIDTH = 256; // horizontal dots  // TODO: maybe use unit checker here
    public static final int ACTIVE_HEIGHT = 240; // vertical dots

    public static final int V_BLANK_START = ACTIVE_HEIGHT + 1; // scan line
    public static final int H_BLANK_START = ACTIVE_WIDTH + 1; // dot / cycle

    private final long masterClock; // Hz // TODO: maybe use unit checker here
    private final int cpuDivisor;
    private final int ppuDivisor;
    private final int physicalHeight; // vertical, scan lines per frame

    VideoStandard(long masterClock, int cpuDiv, int ppuDiv, int physicalHeight) {
        this.masterClock = masterClock;
        this.cpuDivisor = cpuDiv;
        this.ppuDivisor = ppuDiv;
        this.physicalHeight = physicalHeight;
    }

    public double getMasterClock() {
        return (double) masterClock;
    }

    public double getCpuFrequency() {
        return (double) masterClock / cpuDivisor;
    }

    public double getPpuFrequency() {
        return (double) masterClock / ppuDivisor;
    }

    public double getRefreshRate() {
        int dotsPerFrame = physicalHeight * PHYSICAL_WIDTH;
        return getPpuFrequency() / dotsPerFrame;
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

    public int getVerticalBlankStart() {
        return V_BLANK_START;
    }

    public int getHorizontalBlankStart() {
        return H_BLANK_START;
    }
}
