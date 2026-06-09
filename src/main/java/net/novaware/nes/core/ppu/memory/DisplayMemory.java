package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class DisplayMemory implements Nameable {

    private static final int COLOR_MASK = 0x0F;
    private static final int META_MASK = 0xF0;

    private final String name;

    private @Unsigned byte[][] frontBuffer;
    private @Unsigned byte[][] backBuffer;

    public DisplayMemory(String name, int height, int width) {
        this.name = name;

        frontBuffer = new @Unsigned byte[height][width];
        backBuffer = new @Unsigned byte[height][width];
    }

    // TODO: structure: 0bMMMM_CCCC where CCCC is color from palette and MMMM is metadata like layer/zindex/transparency? etc
    // if 4 bits is not enough or to slow just use secondary array of the same size but other type.
    // maybe link back to oam for individual sprite / sprite group

    // TODO: layers: (gemini: NES PPU Pixel layers)
    //  - backdrop (with border region)
    //  - hidden sprites
    //  - background
    //  - visible sprites
    //  - mask/clip (left 8 pixels)
    //  - overscan / bezel (ui side, ppu not involved)

    // TODO: allow multiple instances for handoff between threads

    public int getHeight() {
        return frontBuffer.length;
    }

    public int getWidth() {
        assert frontBuffer.length > 0;

        return frontBuffer[0].length;
    }

    public @Unsigned byte getColor(int y, int x) { // TODO: probably should be synced
        assert y < frontBuffer.length; // TODO: consider hard assertions, but verify performance penalty
        assert 0 < frontBuffer.length & x < frontBuffer[0].length;

        return ubyte(sint(frontBuffer[y][x]) & COLOR_MASK);
    }

    public void setColor(int y, int x, @Unsigned byte color) {
        assert y < backBuffer.length; // TODO: consider hard assertions, but verify performance penalty
        assert 0 < backBuffer.length && x < backBuffer[0].length;

        backBuffer[y][x] = ubyte(sint(color) & COLOR_MASK);
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
        @Unsigned byte[][] swapped = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = swapped;
    }
}
