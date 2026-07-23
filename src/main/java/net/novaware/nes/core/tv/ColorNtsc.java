package net.novaware.nes.core.tv;

/**
 * @see <a href="https://en.wikipedia.org/wiki/NTSC">NTSC on wikipedia.org</a>
 */
public interface ColorNtsc {

    /**
     * Reduction to allow for color
     */
    double RATE_DIVIDER = 1.001;

    double FRAME_RATE = SystemM.FRAME_RATE / RATE_DIVIDER; // Hz
    double FIELD_RATE = SystemM.FIELD_RATE / RATE_DIVIDER; // Hz

    /**
     * Color
     */
    double I_LINE_RATE = SystemM.TOTAL_I_LINES * FRAME_RATE;

    /**
     * 5 x 7 x 9 MHz / (8 x 11)
     */
    double SUBCARRIER = 315.0 * 1_000_000.0 / 88.0; // Hz
}
