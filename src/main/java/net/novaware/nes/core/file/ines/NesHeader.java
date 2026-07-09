package net.novaware.nes.core.file.ines;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NesHeader {

    private NesHeader() {
        // utility / constants class
    }

    public static final int SIZE = 16; // bytes

    public static ByteBuffer allocate() {
        return ByteBuffer.allocate(SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
    }
}
