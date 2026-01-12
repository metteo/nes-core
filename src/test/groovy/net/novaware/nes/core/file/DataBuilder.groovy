package net.novaware.nes.core.file

import net.novaware.nes.core.file.ines.NesFileHandler;

import java.nio.ByteBuffer

import static java.nio.ByteBuffer.allocate;

class DataBuilder {

    private static Random random = new Random();

    private ByteBuffer header;
    private ByteBuffer trainer;
    private ByteBuffer program;
    private ByteBuffer video;
    private ByteBuffer inst;
    private ByteBuffer prom;
    private ByteBuffer remainder;

    // TODO: add methods that fake the data, possibly replacing NesFileFaker all together
    //       or one combo method that accepts Meta and takes sizing from it.

    private static ByteBuffer emptyBuffer() { return allocate(0); }

    private static ByteBuffer randomBuffer(int size) {
        byte[] buffer = new byte[size];
        random.nextBytes(buffer);
        return ByteBuffer.wrap(buffer);
    }

    static DataBuilder emptyData() {
        return new DataBuilder().header(emptyBuffer())
                .trainer(emptyBuffer())
                .program(emptyBuffer())
                .video(emptyBuffer())
                .inst(emptyBuffer())
                .prom(emptyBuffer())
                .remainder(emptyBuffer())
    }

    static DataBuilder marioBros() {
        return emptyData()
                .program(randomBuffer(16 * 1024))
                .video(randomBuffer(8 * 1024));
    }

    static DataBuilder randomData(MetaBuilder metaBuilder) {
        NesFile.Meta meta = metaBuilder.build()
        boolean playChoice10 = meta.system() == NesFile.System.PLAY_CHOICE_10

        return new DataBuilder().header(allocate(NesFileHandler.HEADER_SIZE))
                .trainer(randomBuffer(meta.trainer().toBytes()))
                .program(randomBuffer(meta.programData().toBytes()))
                .video(randomBuffer(meta.videoData().toBytes()))
                .inst(playChoice10 ? randomBuffer(8 * 1024) : emptyBuffer())
                .prom(playChoice10 ? randomBuffer(2 * 16) : emptyBuffer())
                .remainder(randomBuffer(meta.remainder().toBytes())) // TODO: use meta.title to fill?
    }

    DataBuilder header(ByteBuffer header) {
        this.header = header;
        return this;
    }

    DataBuilder trainer(ByteBuffer trainer) {
        this.trainer = trainer;
        return this;
    }

    DataBuilder program(ByteBuffer program) {
        this.program = program;
        return this;
    }

    DataBuilder video(ByteBuffer video) {
        this.video = video;
        return this;
    }

    DataBuilder inst(ByteBuffer inst) {
        this.inst = inst;
        return this;
    }

    DataBuilder prom(ByteBuffer prom) {
        this.prom = prom;
        return this;
    }

    DataBuilder remainder(ByteBuffer remainder) {
        this.remainder = remainder;
        return this;
    }

    NesFile.Data build() {
        return NesFile.Data.builder()
                .header(header)
                .trainer(trainer)
                .program(program)
                .video(video)
                .inst(inst)
                .prom(prom)
                .remainder(remainder)
                .build();
    }
}
