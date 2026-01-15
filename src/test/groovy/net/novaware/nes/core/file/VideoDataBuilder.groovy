package net.novaware.nes.core.file

import net.novaware.nes.core.test.TestDataBuilder
import net.novaware.nes.core.util.QuantityBuilder

class VideoDataBuilder implements TestDataBuilder<NesMeta.VideoData> {

    private NesMeta.Layout layout
    private QuantityBuilder size

    static VideoDataBuilder none() {
        return videoData(NesMeta.Layout.UNKNOWN, 0);

    }

    static VideoDataBuilder vertical(int amount) {
        return videoData(NesMeta.Layout.STANDARD_VERTICAL, amount);

    }

    static VideoDataBuilder videoData(NesMeta.Layout layout, int amount) {
        return new VideoDataBuilder()
                .layout(layout)
                .quantity(QuantityBuilder.banks8kb(amount))

    }

    VideoDataBuilder layout(NesMeta.Layout layout) {
        this.layout = layout
        return this
    }

    VideoDataBuilder quantity(QuantityBuilder size) {
        this.size = size
        return this
    }

    @Override
    NesMeta.VideoData build() {
        return new NesMeta.VideoData(layout, size.build());
    }
}
