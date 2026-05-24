package net.novaware.nes.core.ppu.memory;

import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class DisplayMemory implements Nameable {

    private static final int COLOR_MASK = 0x0F;
    private static final int META_MASK = 0xF0;

    private final String name;

    private final @Unsigned byte[][] buffer;

    public DisplayMemory(String name, int height, int width) {
        this.name = name;

        buffer = new @Unsigned byte[height][width];
    }

    // TODO: structure: 0bMMMM_CCCC where CCCC is color from palette and MMMM is metadata like layer/zindex etc
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
        return buffer.length;
    }

    public int getWidth() {
        assert buffer.length > 0;

        return buffer[0].length;
    }

    public @Unsigned byte getColor(int y, int x) {
        assert y < buffer.length; // TODO: consider hard assertions, but verify performance penalty
        assert 0 < buffer.length & x < buffer[0].length;

        return ubyte(sint(buffer[y][x]) & COLOR_MASK);
    }

    public void setColor(int y, int x, @Unsigned byte color) {
        assert y < buffer.length; // TODO: consider hard assertions, but verify performance penalty
        assert 0 < buffer.length && x < buffer[0].length;

        buffer[y][x] = ubyte(sint(color) & COLOR_MASK);
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
}
