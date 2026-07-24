package net.novaware.nes.core.tv;

/**
 * @see <a href="https://en.wikipedia.org/wiki/CCIR_System_M">System M on wikipedia.org</a>
 */
public interface SystemM {

    /**
     * Interlaced, per iframe
     */
    int TOTAL_I_LINES = 525;

    /**
     * Interlaced, per iframe
     */
    int BLANK_I_LINES = 45;

    /**
     * Interlaced, per iframe
     */
    int ACTIVE_I_LINES = TOTAL_I_LINES - BLANK_I_LINES;

    /**
     * Interlaced, Monochrome
     */
    int I_FRAME_RATE = 30; // Hz

    int FIELDS_PER_FRAME = 2;

    /**
     * Monochrome, see {@link ColorNtsc#FIELD_RATE} for color
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
