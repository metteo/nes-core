package net.novaware.nes.core.ppu.inject;

public enum PpuVarName {

    // region Basic Registers
    CC ("", "cycleCounter",    "Cycle Counter"),
    SC ("", "scanLineCounter", "Scan Line Counter"),
    DC ("", "dotCounter",      "Dot Counter"),

    PS ("STATUS", "ppuStatus",       "PPU Status"),
    HB ("",       "horizontalBlank", "Horizontal Blank"),

    VX ("", "currentViewport", "Current Viewport"),
    T  ("", "tempViewport", "Temporary Viewport"),
    W  ("", "secondWrite", "Second Write"),
    DR ("DATA.R", "dataRead", "Data Read Buffer"),

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

    MS ("MASK.M", "", "Mask Sprite"), // TODO: consider Clip instead of Mask
    MB ("MASK.m", "", "Mask Background"),

    GS ("MASK.G", "", "Greyscale Mode"),

    OF ("", "oddFrame", "Odd Frame"),
    RL ("", "resetLock", "Reset Lock"),

    // endregion

    VRAM ("", "videoMemory", "Video RAM"),
    PAL ("", "paletteMemory", "Palette RAM"),
    BUS ("", "memoryBus", "Memory Bus"),
    DMA ("", "dma", "Direct Memory Access"),
    OAM ("", "oam", "Object Attribute Memory"), // FIXME: There is Primary (64) and Secondary (32) OAM!

    DAM ("", "displayA", "Display A Memory"),
    DBM ("", "displayB", "Display B Memory"),

    VBI ("INT", "vbi", "Vertical Blank INTerrupt"),
    S0H ("", "sprite0Hit", "Sprite 0 Hit"),
    SOV ("", "spriteOverflow", "Sprite Overflow"),
    RST ("", "reset", "Reset"),

    PT0 ("", "pattenTable0", "Pattern Table 0 (L)"),
    PT1 ("", "pattenTable1", "Pattern Table 1 (R)"),

    NT0 ("", "nameTable0", "Name Table 0"),
    NT1 ("", "nameTable1", "Name Table 1"),
    NT2 ("", "nameTable2", "Name Table 2"),
    NT3 ("", "nameTable3", "Name Table 3"),

    AT0 ("", "attributeTable0", "Attribute Table 0"),
    AT1 ("", "attributeTable1", "Attribute Table 1"),
    AT2 ("", "attributeTable2", "Attribute Table 2"),
    AT3 ("", "attributeTable3", "Attribute Table 3"),
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
