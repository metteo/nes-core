package net.novaware.nes.core.file

import net.novaware.nes.core.net.UriBuilder
import net.novaware.nes.core.test.TestDataBuilder

class NesFileBuilder implements TestDataBuilder<NesFile> {

    private UriBuilder origin = UriBuilder.marioBros()
    private NesMetaBuilder meta = NesMetaBuilder.marioBros()
    private NesDataBuilder data = NesDataBuilder.marioBros()

    // TODO: separate clean archaic marioBros from dirty one (with info and title)
    static NesFileBuilder marioBros() {
        return new NesFileBuilder()
    }

    static NesFileBuilder complexNesFile() {
        def meta = NesMetaBuilder.complexMeta()
        return new NesFileBuilder()
            .meta(meta)
            .data(NesDataBuilder.randomData(meta))
    }

    NesFileBuilder origin(UriBuilder origin) {
        this.origin = origin
        return this
    }

    NesFileBuilder meta(NesMetaBuilder meta) {
        this.meta = meta
        return this
    }

    NesMetaBuilder meta() {
        return this.meta
    }

    NesFileBuilder data(NesDataBuilder data) {
        this.data = data
        return this
    }

    NesDataBuilder data() {
        return this.data
    }

    @Override
    NesFile build() {
        // TODO: calculate hash based on data
        return new NesFile(origin.build(), meta.build(), data.build(), NesHash.empty())
    }
}
