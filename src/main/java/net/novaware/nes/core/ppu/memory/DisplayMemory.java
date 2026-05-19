package net.novaware.nes.core.ppu.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

public class DisplayMemory {

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
    // TODO: sizing should depend on VideoStandard active dimensions

    public int getHeight() {
        return 0;
    }

    public int getWidth() {
        return 0;
    }

    public @Unsigned byte getColor(int y, int x) {
        return 0; // TODO: implement
    }

    public void setColor(int y, int x, @Unsigned byte color) {
        // TODO: implement
    }

    public @Unsigned byte getMeta(int y, int x) { // TODO: consider dedicated methods per info like enum with layers
        return 0; // TODO: implement
    }

    public void setMeta(int y, int x, @Unsigned byte meta) {
        // TODO: implement
    }
}
