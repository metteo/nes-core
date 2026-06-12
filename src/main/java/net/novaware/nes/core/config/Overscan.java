package net.novaware.nes.core.config;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum Overscan {
    NTSC (VideoStandard.NTSC, 16, 11, 8, 8), // TODO: consider border region
    PAL  (VideoStandard.PAL,   2,  2, 2, 2),

    // TODO: add other video standards

    UNKNOWN (VideoStandard.UNKNOWN, -1, -1, -1, -1),
    ;
    private final VideoStandard videoStandard;

    private final int left;
    private final int right;
    private final int top;
    private final int bottom;

    private static Map<VideoStandard, Overscan> byVideoStandardIndex = Stream.of(values())
            .collect(toMap(Overscan::getVideoStandard, Function.identity()));

    Overscan(VideoStandard videoStandard, int left, int right, int top, int bottom) {
        this.videoStandard = videoStandard;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public static Overscan of(VideoStandard vs) {
        return byVideoStandardIndex.getOrDefault(vs, UNKNOWN);
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public VideoStandard getVideoStandard() {
        return videoStandard;
    }
}
