package net.novaware.nes.core.file;

class NesFileBuilder implements TestDataBuilder<NesFile> {

    public static final String DEFAULT_ORIGIN = "/home/user/Mario_Bros.nes";

    private String origin = DEFAULT_ORIGIN;
    private MetaBuilder meta = MetaBuilder.marioBros();
    private DataBuilder data = DataBuilder.marioBros();

    static NesFileBuilder marioBros() {
        return new NesFileBuilder();
    }

    NesFileBuilder origin(String origin) {
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
        return new NesFile(origin, meta.build(), data.build());
    }
}
