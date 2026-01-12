package net.novaware.nes.core.file

import net.novaware.nes.core.test.TestDataBuilder;

class NesFileBuilder implements TestDataBuilder<NesFile> {

    public static final URI DEFAULT_ORIGIN = URI.create("file:///home/user/Mario_Bros.nes")

    private URI origin = DEFAULT_ORIGIN
    private MetaBuilder meta = MetaBuilder.marioBros()
    private DataBuilder data = DataBuilder.marioBros()

    static NesFileBuilder marioBros() {
        return new NesFileBuilder();
    }

    static NesFileBuilder complexNesFile() {
        def meta = MetaBuilder.complexMeta()
        return new NesFileBuilder()
            .meta(meta)
            .data(DataBuilder.randomData(meta));
    }

    NesFileBuilder origin(URI origin) {
        this.origin = origin;
        return this;
    }

    NesFileBuilder meta(MetaBuilder meta) {
        this.meta = meta;
        return this;
    }

    NesFileBuilder data(DataBuilder data) {
        this.data = data;
        return this;
    }

    @Override
    NesFile build() {
        // TODO: calculate hash based on data
        return new NesFile(origin, meta.build(), data.build(), NesHash.empty())
    }
}
