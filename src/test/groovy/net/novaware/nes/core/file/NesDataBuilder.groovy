package net.novaware.nes.core.file

import net.novaware.nes.core.file.ines.NesFileHandler
import net.novaware.nes.core.file.ines.NesHeader
import net.novaware.nes.core.test.TestDataBuilder

import java.nio.ByteBuffer

import static java.nio.ByteBuffer.allocate

class NesDataBuilder implements TestDataBuilder<NesData> {

    private static Random random = new Random()

    private ByteBuffer header;
    private ByteBuffer trainer;
    private ByteBuffer program;
    private ByteBuffer video;
    private ByteBuffer misc;
    private ByteBuffer footer;

    // TODO: add methods that fake the data, possibly replacing NesFileFaker all together
    //       or one combo method that accepts Meta and takes sizing from it.

    private static ByteBuffer emptyBuffer() { return allocate(0); }

    private static ByteBuffer randomBuffer(int size) {
        byte[] buffer = new byte[size];
        random.nextBytes(buffer);
        return ByteBuffer.wrap(buffer);
    }

    static NesDataBuilder emptyData() {
        return new NesDataBuilder().header(emptyBuffer())
                .trainer(emptyBuffer())
                .program(emptyBuffer())
                .video(emptyBuffer())
                .misc(emptyBuffer())
                .footer(emptyBuffer())
    }

    static NesDataBuilder marioBros() {
        return emptyData()
                .program(randomBuffer(16 * 1024))
                .video(randomBuffer(8 * 1024));
    }

    static NesDataBuilder randomData(NesMetaBuilder metaBuilder) {
        NesMeta meta = metaBuilder.build()
        boolean playChoice10 = meta.system() == NesMeta.System.PLAY_CHOICE_10

        return new NesDataBuilder().header(allocate(NesHeader.SIZE))
                .trainer(randomBuffer(meta.trainer().toBytes()))
                .program(randomBuffer(meta.programData().toBytes()))
                .video(randomBuffer(meta.videoData().size().toBytes()))
                .misc(playChoice10 ? randomBuffer(8 * 1024 + 2 * 16) : emptyBuffer())
                .footer(randomBuffer(meta.footer().toBytes())) // TODO: use meta.title to fill?
    }

    NesDataBuilder header(ByteBuffer header) {
        this.header = header;
        return this;
    }

    NesDataBuilder trainer(ByteBuffer trainer) {
        this.trainer = trainer;
        return this;
    }

    NesDataBuilder program(ByteBuffer program) {
        this.program = program;
        return this;
    }

    NesDataBuilder video(ByteBuffer video) {
        this.video = video;
        return this;
    }

    NesDataBuilder misc(ByteBuffer misc) {
        this.misc = misc;
        return this;
    }

    NesDataBuilder footer(ByteBuffer footer) {
        this.footer = footer;
        return this;
    }

    NesData build() {
        return NesData.builder()
                .header(header)
                .trainer(trainer)
                .program(program)
                .video(video)
                .misc(misc)
                .footer(footer)
                .build();
    }
}
