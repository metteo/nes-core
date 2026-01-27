package net.novaware.nes.core.file.ines;

import net.novaware.nes.core.util.UByteBuffer;

import java.nio.ByteOrder;

public final class NesHeader {

    private NesHeader() {
        // utility / constants class
    }

    public static final int SIZE = 16; // bytes

    public static UByteBuffer allocate() {
        return UByteBuffer.allocate(SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
    }
}
