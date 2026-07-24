package net.novaware.nes.core.tv;

/**
 * @see <a href="https://en.wikipedia.org/wiki/CCIR_System_B">System B on wikipedia.org</a>
 */
public interface SystemB {

    /**
     * Interlaced, per iframe
     */
    int TOTAL_I_LINES = 625;

    /**
     * Interlaced, per iframe
     */
    int BLANK_I_LINES = 49;

    /**
     * Interlaced, per iframe
     */
    int ACTIVE_I_LINES = TOTAL_I_LINES - BLANK_I_LINES;

    /**
     * Interlaced, Monochrome
     */
    int I_FRAME_RATE = 25; // Hz

    int FIELDS_PER_FRAME = 2;

    /**
     * Monochrome, see {@link ColorPal#FIELD_RATE} for color
     *
     * @see <a href="https://en.wikipedia.org/wiki/Field_(video)">Field on wikipedia.org</a>
     */
    int FIELD_RATE = I_FRAME_RATE * FIELDS_PER_FRAME; // Hz

    /**
     * Monochrome
     */
    int I_LINE_RATE = TOTAL_I_LINES * I_FRAME_RATE; // Hz

    /**
     * Progressive, per pframe
     */
    int TOTAL_P_LINES = TOTAL_I_LINES / FIELDS_PER_FRAME; // truncate on purpose

    /**
     * Progressive, per pframe
     */
    int BLANK_P_LINES = BLANK_I_LINES / FIELDS_PER_FRAME; // truncate on purpose

    /**
     * Progressive, per pframe
     */
    int ACTIVE_P_LINES = ACTIVE_I_LINES / FIELDS_PER_FRAME; // truncate on purpose

    /**
     * Progressive
     */
    int P_FRAME_RATE = FIELD_RATE;
}
