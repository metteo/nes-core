package net.novaware.nes.core.tv;

/**
 * PAL B/G/H/I/D/K/L used around the World
 *
 * @see <a href="https://en.wikipedia.org/wiki/PAL">PAL on wikipedia.org</a>
 */
public interface ColorPal {

    /**
     * No reduction for color
     */
    double RATE_DIVIDER = 1.0;

    double I_FRAME_RATE = SystemB.I_FRAME_RATE / RATE_DIVIDER; // Hz
    double FIELD_RATE   = SystemB.FIELD_RATE   / RATE_DIVIDER; // Hz
    double P_FRAME_RATE = FIELD_RATE; // Hz

    /**
     * Color
     */
    double I_LINE_RATE = SystemB.TOTAL_I_LINES * I_FRAME_RATE; // Hz

    /**
     * Also 1135 / 4 * H + 25
     */
    double SUBCARRIER = 283.75 * I_LINE_RATE + 25; // Hz
}
