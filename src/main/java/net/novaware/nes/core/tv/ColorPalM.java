package net.novaware.nes.core.tv;

/**
 * @see <a href="https://en.wikipedia.org/wiki/PAL-M">PAL M on wikipedia.org</a>
 */
public interface ColorPalM {

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
     * Also 8181 / 2288 MHz
     */
    double SUBCARRIER = 227.25 * I_LINE_RATE; // Hz
}
