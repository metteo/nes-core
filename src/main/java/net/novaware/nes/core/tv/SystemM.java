package net.novaware.nes.core.tv;

/**
 * @see <a href="https://en.wikipedia.org/wiki/CCIR_System_M">System M on wikipedia.org</a>
 */
public interface SystemM {

    /**
     * Interlaced, per frame
     */
    int TOTAL_I_LINES = 525;

    /**
     * Interlaced, per frame
     */
    int BLANK_I_LINES = 45;

    /**
     * Interlaced, per frame
     */
    int ACTIVE_I_LINES = TOTAL_I_LINES - BLANK_I_LINES;

    /**
     * Monochrome
     */
    int FRAME_RATE = 30; // Hz

    /**
     * Monochrome
     */
    int I_LINE_RATE = TOTAL_I_LINES * FRAME_RATE; // Hz

    /**
     * Progressive, per field
     */
    int TOTAL_P_LINES = TOTAL_I_LINES / 2; // truncate on purpose

    /**
     * Progressive, per field
     */
    int BLANK_P_LINES = BLANK_I_LINES / 2; // truncate on purpose

    /**
     * Progressive, per field
     */
    int ACTIVE_P_LINES = ACTIVE_I_LINES / 2; // truncate on purpose

    /**
     * Monochrome, see {@link ColorNtsc#FIELD_RATE} for color
     */
    int FIELD_RATE = FRAME_RATE * 2; // Hz
}
