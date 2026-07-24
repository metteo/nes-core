package net.novaware.nes.core.tv;

/**
 * PAL B/G/H/I/D/K/L in {@link ColorPal}
 * PAL M is here
 * PAL N is not supported yet.
 *
 * @see <a href="https://en.wikipedia.org/wiki/PAL-M">PAL M on wikipedia.org</a>
 */
public interface ColorPalM {

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
     * Also 8181 / 2288 MHz
     */
    double SUBCARRIER = 227.25 * I_LINE_RATE; // Hz
}
