package net.novaware.nes.core.file


import net.novaware.nes.core.file.ines.NesHeader
import net.novaware.nes.core.test.TestDataBuilder
import net.novaware.nes.core.util.UByteBuffer

import java.nio.ByteBuffer

import static java.nio.ByteBuffer.allocate
import static java.nio.ByteOrder.LITTLE_ENDIAN
import static net.novaware.nes.core.util.UTypes.ubyte

class NesDataBuilder implements TestDataBuilder<NesData> {

    private static Random random = new Random()

    private UByteBuffer header;
    private ByteBuffer trainer;
    private ByteBuffer program;
    private ByteBuffer video;
    private ByteBuffer misc;
    private ByteBuffer footer;

    // TODO: add methods that fake the data, possibly replacing NesFileFaker all together
    //       or one combo method that accepts Meta and takes sizing from it.

    private static ByteBuffer emptyBuffer() { return allocate(0).order(LITTLE_ENDIAN) }

    private static ByteBuffer randomBuffer(int size) {
        byte[] buffer = new byte[size];
        random.nextBytes(buffer); // TODO: use watermarking e.g. 0b0101_0101 (0x55), 0b10101010 (0xAA), (index % 256)
        return ByteBuffer.wrap(buffer).order(LITTLE_ENDIAN)
    }

    static NesDataBuilder nesData() {
        return new NesDataBuilder()
    }

    static NesDataBuilder emptyData() {
        return new NesDataBuilder().header(UByteBuffer.empty())
                .trainer(emptyBuffer())
                .program(emptyBuffer())
                .video(emptyBuffer())
                .misc(emptyBuffer())
                .footer(emptyBuffer())
    }

    static NesDataBuilder marioBros() {
        return emptyData()
                // TODO: possibly header too
                .program(randomBuffer(16 * 1024))
                .video(randomBuffer(8 * 1024))
                // TODO: and footer
    }

    static NesDataBuilder randomData(NesMetaBuilder metaBuilder) {
        NesMeta meta = metaBuilder.build()
        boolean playChoice10 = meta.system() == NesMeta.System.PLAY_CHOICE_10

        return new NesDataBuilder().header(UByteBuffer.allocate(NesHeader.SIZE))
                .trainer(randomBuffer(meta.trainer().toBytes()))
                .program(randomBuffer(meta.programData().toBytes()))
                .video(randomBuffer(meta.videoData().size().toBytes()))
                .misc(playChoice10 ? randomBuffer(8 * 1024 + 2 * 16) : emptyBuffer())
                .footer(randomBuffer(meta.footer().toBytes())) // TODO: use meta.title to fill?
    }

    static NesDataBuilder watermarkedData() {
        return nesData()
                .header(NesHeader.allocate().fill(ubyte(0xAA)))
                .trainer(UByteBuffer.allocate(512).fill(ubyte(0x88)).unwrap())
                .program(UByteBuffer.allocate(16 * 1024).fill(ubyte(0x55)).unwrap())
                .video(UByteBuffer.allocate(8 * 1024).fill(ubyte(0x88)).unwrap())
                .misc(UByteBuffer.allocate(8 * 1024 + 2 * 16).fill(ubyte(0xCC)).unwrap())
                .footer(UByteBuffer.allocate(127).fill(ubyte(0xF0)).unwrap())
    }

    NesDataBuilder header(UByteBuffer header) {
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
