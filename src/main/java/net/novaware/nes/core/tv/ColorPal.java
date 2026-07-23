package net.novaware.nes.core.tv;

/**
 * PAL B/G/H/I/D/K/L.
 * PAL M has a dedicated class.
 * PAL N is not supported yet.
 *
 * @see <a href="https://en.wikipedia.org/wiki/PAL">PAL on wikipedia.org</a>
 */
public interface ColorPal {

    /**
     * No reduction for color
     */
    double RATE_DIVIDER = 1.0;

    double FRAME_RATE = SystemB.FRAME_RATE / RATE_DIVIDER; // Hz
    double FIELD_RATE = SystemB.FIELD_RATE / RATE_DIVIDER; // Hz

    /**
     * Color
     */
    double I_LINE_RATE = SystemB.TOTAL_I_LINES * FRAME_RATE;

    /**
     *
     */
    double SUBCARRIER = 283.75 * I_LINE_RATE + 25; // Hz
}
