package net.novaware.nes.core.tv;

import java.util.List;

import static net.novaware.nes.core.tv.SystemTV.B;
import static net.novaware.nes.core.tv.SystemTV.M;
import static net.novaware.nes.core.tv.SystemTV.N;
import static net.novaware.nes.core.tv.SystemTV.U;

/**
 * Color TV encodings / formats / standards
 *
 * @see <a href="https://web.archive.org/web/20120805204049/http://www.pembers.freeserve.co.uk/World-TV-Standards/index.html">World Analogue TV Standards</a>
 */
public enum ColorTV {
    NTSC  (M, ColorNtsc.I_FRAME_RATE, ColorNtsc.P_FRAME_RATE, ColorNtsc.SUBCARRIER),
    PAL_M (M, ColorPalM.I_FRAME_RATE, ColorPalM.P_FRAME_RATE, ColorPalM.SUBCARRIER),

    PAL_N (N, ColorPalN.I_FRAME_RATE, ColorPalN.P_FRAME_RATE, ColorPalN.SUBCARRIER),
    PAL   (B, ColorPal .I_FRAME_RATE, ColorPal .P_FRAME_RATE, ColorPal .SUBCARRIER),

    UNKNOWN (U, -1d, -1d, -1d)
    ;

    private static final List<ColorTV> instances = List.of(values());

    private final SystemTV system;
    private final double interlacedFrameRate;
    private final double progressiveFrameRate;
    private final double subcarrier;

    ColorTV(
        SystemTV system,
        double interlacedFrameRate,
        double progressiveFrameRate,
        double subcarrier
    ) {
        this.system = system;
        this.interlacedFrameRate = interlacedFrameRate;
        this.progressiveFrameRate = progressiveFrameRate;
        this.subcarrier = subcarrier;
    }

    public SystemTV getSystem() {
        return system;
    }

    public double getInterlacedFrameRate() {
        return interlacedFrameRate;
    }

    public double getProgressiveFrameRate() {
        return progressiveFrameRate;
    }

    public double getSubcarrier() {
        return subcarrier;
    }

    @Override
    public String toString() {
        return name().replace("_", " ");
    }

    public String toText() {
        String text = String.format("%-5s: %si/%.2f %sp%.2f Fsc=%.2f",
            this,
            system.getActiveInterlacedLines(), getInterlacedFrameRate(),
            system.getActiveProgressiveLines(), getProgressiveFrameRate(),
            getSubcarrier()
        );

        return text;
    }

    /**
     * Utility to print Color TV standards to console
     */
    static void main() {
        instances.stream()
                .filter(c -> c != UNKNOWN)
                .map(ColorTV::toText)
                .forEach(s -> System.out.println(s)); // NOTE: lambda will cause checker "Incompatible receiver type"
    }
}
