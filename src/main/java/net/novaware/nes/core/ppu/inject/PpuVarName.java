package net.novaware.nes.core.ppu.inject;

public enum PpuVarName {

    // region Basic Registers
    CC ("", "cycleCounter", "Cycle Counter"),

    FC ("", "frameCounter", "Frame Counter"),
    FT ("", "frameToggle", "Frame Toggle"),
    LC ("", "lineCounter",  "Line Counter"),
    DC ("", "dotCounter",   "Dot Counter"),

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

    RL ("", "resetLock", "Reset Lock"), // TODO: rename to init frame / phase / register lock?

    // endregion

    VRAM ("", "videoMemory", "Video RAM"),
    VOUT ("", "videoOut", "Video OUT"),

    PAL ("", "paletteMemory", "Palette RAM"),
    BUS ("", "memoryBus", "Memory Bus"),
    DMA ("", "dma", "Direct Memory Access"),

    //OAM ("", "oam", "Object Attribute Memory"),
    POA ("OAM.PRI", "priObjAttr", "Primary OAM"),
    SOA ("OAM.SEC", "secObjAttr", "Secondary OAM"),

    VBI ("INT", "vbi", "Vertical Blank INTerrupt"),
    S0H ("", "sprite0Hit", "Sprite 0 Hit"),
    SOV ("", "spriteOverflow", "Sprite Overflow"),
    RST ("", "reset", "Reset"),

    PTS ("", "patternTables", "Pattern Tables"),
    PT0 ("", "patternTable0", "Pattern Table 0 (L)"),
    PT1 ("", "patternTable1", "Pattern Table 1 (R)"),

    LT0("", "layoutTable0", "Layout Table 0"),
    LT1("", "layoutTable1", "Layout Table 1"),
    LT2("", "layoutTable2", "Layout Table 2"),
    LT3("", "layoutTable3", "Layout Table 3"),

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
