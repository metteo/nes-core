package net.novaware.nes.core.tv;

/**
 * NTSC (M) used in Americas and Japan
 *
 * @see <a href="https://en.wikipedia.org/wiki/NTSC">NTSC on wikipedia.org</a>
 */
public interface ColorNtsc {

    /**
     * Reduction to allow for color
     */
    double RATE_DIVIDER = 1.001;

    double I_FRAME_RATE = SystemM.I_FRAME_RATE / RATE_DIVIDER; // Hz
    double FIELD_RATE   = SystemM.FIELD_RATE   / RATE_DIVIDER; // Hz
    double P_FRAME_RATE = FIELD_RATE; // Hz

    /**
     * Color
     */
    double I_LINE_RATE = SystemM.TOTAL_I_LINES * I_FRAME_RATE; // Hz

    /**
     * Also 5 x 7 x 9 MHz / (8 x 11) OR 315 / 88 OR 910 / 4 * H
     */
    double SUBCARRIER = 227.5 * I_LINE_RATE; // Hz
}
