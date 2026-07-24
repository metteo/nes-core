package net.novaware.nes.core.tv;

/**
 * PAL N used in Argentina
 *
 * @see <a href="https://en.wikipedia.org/wiki/PAL#PAL-N_(Argentina,_Paraguay,_and_Uruguay)">PAL N on wikipedia.org</a>
 */
public interface ColorPalN {

    /**
     * No reduction for color
     */
    double RATE_DIVIDER = 1.0;

    double I_FRAME_RATE = SystemN.I_FRAME_RATE / RATE_DIVIDER; // Hz
    double FIELD_RATE   = SystemN.FIELD_RATE   / RATE_DIVIDER; // Hz
    double P_FRAME_RATE = FIELD_RATE; // Hz

    /**
     * Color
     */
    double I_LINE_RATE = SystemN.TOTAL_I_LINES * I_FRAME_RATE; // Hz

    /**
     * Also 917 / 4 * H
     */
    double SUBCARRIER = 229.25 * I_LINE_RATE + 25; // Hz
}
