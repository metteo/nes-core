package net.novaware.nes.core.ppu.inject;

public enum PpuVarName {

    // region Basic Registers
    CC ("",       "cycleCounter",    "Cycle Counter"),
    PS ("STATUS", "ppuStatus",       "PPU Status"),
    HB ("",       "horizontalBlank", "Horizontal Blank"),

    VX ("", "currentViewport", "Current Viewport"),
    T  ("", "tempViewport", "Temporary Viewport"),
    W  ("", "secondWrite", "Second Write"),

    CV ("CTRL.V", "", "Vertical Blank Interrupt Enabled"),
    CP ("CTRL.P", "", "Master / Slave Select"),
    CH ("CTRL.H", "", "Sprite Size"),
    CB ("CTRL.B", "", "Background Pattern Table"),
    CS ("CTRL.S", "", "Sprite Pattern Table"),
    CI ("CTRL.I", "", "VRAM Address Increment"),

    ER ("MASK.R", "", "Emphasize Red"),
    EG ("MASK.G", "", "Emphasize Green"),
    EB ("MASK.B", "", "Emphasize Blue"),

    RS ("MASK.s", "", "Render Sprite"),
    RB ("MASK.b", "", "Render Background"),

    MS ("MASK.M", "", "Mask Sprite"),
    MB ("MASK.m", "", "Mask Background"),

    GS ("MASK.G", "", "Greyscale Mode"),

    // endregion

    VRAM ("", "videoMemory", "Video RAM"),
    PALETTE ("", "paletteMemory", "Palette RAM"),

    BUS ("", "memoryBus", "Memory Bus"),
    DMA ("", "dma", "Direct Memory Access"),

    ;
    private final String doc;
    private final String var;
    private final String display;

    PpuVarName(String doc, String var, String display) {
        this.doc = doc;
        this.var = var;
        this.display = display;
    }

    /**
     * @return documentation specific name (fall back to enum name)
     */
    public String doc() {
        return "PPU." + (doc.isEmpty() ? name() : doc);
    }

    /**
     * @return variable name
     */
    public String var() {
        return var;
    }

    /**
     * @return display name in English
     */
    public String display() {
        return display;
    }
}
