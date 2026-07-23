package net.novaware.nes.core.tv;

/**
 * @see <a href="https://en.wikipedia.org/wiki/CCIR_System_B">System B on wikipedia.org</a>
 */
public interface SystemB {

    /**
     * Interlaced, per frame
     */
    int TOTAL_I_LINES = 625;

    /**
     * Interlaced, per frame
     */
    int BLANK_I_LINES = 49;

    /**
     * Interlaced, per frame
     */
    int ACTIVE_I_LINES = TOTAL_I_LINES - BLANK_I_LINES;

    /**
     * Monochrome
     */
    int FRAME_RATE = 25; // Hz

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
     * Monochrome
     */
    int FIELD_RATE = FRAME_RATE * 2; // Hz
}
