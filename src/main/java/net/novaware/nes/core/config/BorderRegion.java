package net.novaware.nes.core.config;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum BorderRegion { // TODO: remove. waste of memory and screen space? Maybe make it part of the frontend
    NTSC (VideoStandard.NTSC, 16, 11, 0, 2),
    PAL  (VideoStandard.PAL,   2,  2, 1, 0),

    // TODO: add other video standards

    UNKNOWN (VideoStandard.UNKNOWN, -1, -1, -1, -1),
    ;
    private final VideoStandard videoStandard;

    private final int left;
    private final int right;
    private final int top;
    private final int bottom;

    private static Map<VideoStandard, BorderRegion> byVideoStandardIndex = Stream.of(values())
            .collect(toMap(BorderRegion::getVideoStandard, Function.identity()));

    BorderRegion(VideoStandard videoStandard, int left, int right, int top, int bottom) {
        this.videoStandard = videoStandard;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public static BorderRegion of(VideoStandard vs) {
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
