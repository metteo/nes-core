package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.file.NesMeta.VideoStandard;

import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.NTSC_DUAL;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.PAL;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.PAL_DUAL;
import static net.novaware.nes.core.file.NesMeta.VideoStandard.UNKNOWN;
import static net.novaware.nes.core.util.Asserts.assertArgument;

/**
 * Resolves the Video Standard (NTSC, PAL, Dendy) and Dual-Compatibility flags.
 * <h3>Preserving Metadata</h3>
 * This resolver maintains distinct {@code NTSC_DUAL} and {@code PAL_DUAL}
 * types to allow for lossless round-trip header writing.
 * <ul>
 * <li><b>NTSC_DUAL:</b> Maps to bitmask {@code 01} in unofficial iNES Byte 10.
 * Indicates NTSC-base timing with PAL compatibility.</li>
 * <li><b>PAL_DUAL:</b> Maps to bitmask {@code 11} in unofficial iNES Byte 10.
 * Indicates PAL-base timing with NTSC compatibility.</li>
 * </ul>
 */
public class VideoStandardResolver implements Resolver<VideoStandard> {

    VideoStandard resolve(VideoStandard basic, VideoStandard extended) {
        assertArgument(basic == NTSC || basic == PAL || basic == UNKNOWN, () -> "basic video standard must be NTSC or PAL, was: " + basic);
        assertArgument(extended != null, "extended video standard cannot be null");

        // promote to dual
        if (basic == NTSC && (extended == NTSC_DUAL || extended == PAL_DUAL)) {
            return NTSC_DUAL;
        }

        if (basic == PAL && (extended == PAL_DUAL || extended == NTSC_DUAL)) {
            return PAL_DUAL;
        }

        // fallback to basic, even if unknown
        return basic;
    }
}
