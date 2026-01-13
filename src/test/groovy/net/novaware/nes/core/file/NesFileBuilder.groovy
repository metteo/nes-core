package net.novaware.nes.core.file

import net.novaware.nes.core.test.TestDataBuilder;

class NesFileBuilder implements TestDataBuilder<NesFile> {

    public static final URI DEFAULT_ORIGIN = URI.create("file:///home/user/Mario_Bros.nes")

    private URI origin = DEFAULT_ORIGIN
    private NesMetaBuilder meta = NesMetaBuilder.marioBros()
    private NesDataBuilder data = NesDataBuilder.marioBros()

    static NesFileBuilder marioBros() {
        return new NesFileBuilder();
    }

    static NesFileBuilder complexNesFile() {
        def meta = NesMetaBuilder.complexMeta()
        return new NesFileBuilder()
            .meta(meta)
            .data(NesDataBuilder.randomData(meta));
    }

    NesFileBuilder origin(URI origin) {
        this.origin = origin;
        return this;
    }

    NesFileBuilder meta(NesMetaBuilder meta) {
        this.meta = meta;
        return this;
    }

    NesFileBuilder data(NesDataBuilder data) {
        this.data = data;
        return this;
    }

    @Override
    NesFile build() {
        // TODO: calculate hash based on data
        return new NesFile(origin, meta.build(), data.build(), NesHash.empty())
    }
}
