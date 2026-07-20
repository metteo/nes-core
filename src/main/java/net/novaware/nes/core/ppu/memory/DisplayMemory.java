package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.Arrays;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

/**
 * Early prototype
 */
public class DisplayMemory implements Nameable {

    private static final int VALUE_MASK = 0b11_0000;
    private static final int HUE_MASK   = 0b11_1111;
    private static final int COLOR_MASK = VALUE_MASK | HUE_MASK;
    private static final int META_MASK  = 0b1100_0000;

    private final String name;
    private final int height;
    private final int width;

    private @Unsigned byte[] frontBuffer;
    private @Unsigned byte[] backBuffer;

    public DisplayMemory(String name, int height, int width) {
        this.name = name;
        this.height = height;
        this.width = width;

        frontBuffer = new @Unsigned byte[height * width];
        backBuffer = new @Unsigned byte[height * width];
    }

    // TODO: structure: 0bMMCC_CCCC where CC_CCCC is color from palette and MM is metadata like layer/zindex/transparency? etc
    // if 2 bits is not enough or to slow just use secondary array of the same size but other type.
    // maybe link back to oam for individual sprite / sprite group

    // TODO: layers: (gemini: NES PPU Pixel layers)
    //  - backdrop (with border region) 0b00
    //  - hidden sprites                0b01
    //  - background                    0b10
    //  - visible sprites               0b11

    // TODO: allow multiple instances for handoff between threads

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public @Unsigned byte getColor(int y, int x) { // TODO: probably should be synced
        assert y < height; // TODO: consider hard assertions, but verify performance penalty
        assert x < width;

        return ubyte(sint(frontBuffer[y * width + x]) & COLOR_MASK);
    }

    public void setColor(int y, int x, @Unsigned byte color) {
        assert y < height; // TODO: consider hard assertions, but verify performance penalty
        assert x < width;

        backBuffer[y * width + x] = ubyte(sint(color) & COLOR_MASK);
    }

    public void setColor(@Unsigned byte color) {
        Arrays.fill(backBuffer, ubyte(sint(color) & COLOR_MASK));
    }

    public @Unsigned byte getMeta(int y, int x) { // TODO: consider dedicated methods per info like enum with layers
        return 0; // TODO: implement
    }

    public void setMeta(int y, int x, @Unsigned byte meta) {
        // TODO: implement
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": " + getWidth() + "x" + getHeight();
    }

    public synchronized void swap() {
        @Unsigned byte[] swapped = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = swapped;
    }
}
