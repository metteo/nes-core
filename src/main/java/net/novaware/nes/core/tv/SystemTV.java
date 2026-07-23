package net.novaware.nes.core.tv;

import java.util.List;

/**
 * Analog TV systems
 *
 * @see <a href="https://en.wikipedia.org/wiki/Broadcast_television_systems">TV Systems on wikipedia.org</a>
 */
public enum SystemTV {
    M (
        SystemM.TOTAL_I_LINES,
        SystemM.BLANK_I_LINES,
        SystemM.ACTIVE_I_LINES,
        SystemM.FRAME_RATE,
        SystemM.TOTAL_P_LINES,
        SystemM.BLANK_P_LINES,
        SystemM.ACTIVE_P_LINES,
        SystemM.FIELD_RATE
    ),
    B (
        SystemB.TOTAL_I_LINES,
        SystemB.BLANK_I_LINES,
        SystemB.ACTIVE_I_LINES,
        SystemB.FRAME_RATE,
        SystemB.TOTAL_P_LINES,
        SystemB.BLANK_P_LINES,
        SystemB.ACTIVE_P_LINES,
        SystemB.FIELD_RATE
    ),
    U (-1, -1, -1, -1, -1, -1, -1, -1); // Unknown, known used only letters, A-N

    private static final List<SystemTV> instances = List.of(values());

    private final int totalInterlacedLines;
    private final int blankInterlacedLines;
    private final int activeInterlacedLines;
    private final int frameRate;
    private final int totalProgressiveLines;
    private final int blankProgressiveLines;
    private final int activeProgressiveLines;
    private final int fieldRate;

    SystemTV(
        int totalInterlacedLines,
        int blankInterlacedLines,
        int activeInterlacedLines,
        int frameRate,
        int totalProgressiveLines,
        int blankProgressiveLines,
        int activeProgressiveLines,
        int fieldRate
    ) {
        this.totalInterlacedLines = totalInterlacedLines;
        this.blankInterlacedLines = blankInterlacedLines;
        this.activeInterlacedLines = activeInterlacedLines;
        this.frameRate = frameRate;
        this.totalProgressiveLines = totalProgressiveLines;
        this.blankProgressiveLines = blankProgressiveLines;
        this.activeProgressiveLines = activeProgressiveLines;
        this.fieldRate = fieldRate;
    }

    public int getTotalInterlacedLines() {
        return totalInterlacedLines;
    }

    public int getBlankInterlacedLines() {
        return blankInterlacedLines;
    }

    public int getActiveInterlacedLines() {
        return activeInterlacedLines;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getTotalProgressiveLines() {
        return totalProgressiveLines;
    }

    public int getBlankProgressiveLines() {
        return blankProgressiveLines;
    }

    public int getActiveProgressiveLines() {
        return activeProgressiveLines;
    }

    public int getFieldRate() {
        return fieldRate;
    }

    @Override
    public String toString() {
        return "System " + name();
    }

    public String toText() {
        return this + ": " +
            getActiveInterlacedLines() + "i" + getFrameRate() + " " +
            getActiveProgressiveLines() + "p" + getFieldRate();
    }

    /**
     * Utility to print TV Systems to console
     */
    static void main() {
        instances.stream()
                .filter(vs -> vs != U)
                .map(SystemTV::toText)
                .forEach(s -> System.out.println(s)); // NOTE: lambda will cause checker "Incompatible receiver type"
    }
}
