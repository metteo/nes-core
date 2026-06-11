package net.novaware.nes.core.util;

public class BitSlot {
    private final int mask;
    private final int shift;

    public BitSlot(int mask) {
        this.mask = mask;
        this.shift = Integer.numberOfTrailingZeros(mask);
    }

    public int get(int input) {
        return (input & mask) >> shift;
    }

    public int set(int input, int value) {
        int unsetInput = input & ~mask;
        int maskedValue = (value << shift) & mask;
        return unsetInput | maskedValue;
    }

    // TODO: make methods for ubyte, ushort and tests
}
