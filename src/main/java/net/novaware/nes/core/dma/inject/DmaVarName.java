package net.novaware.nes.core.dma.inject;

public enum DmaVarName {

    CC ("", "cycleCounter", "Cycle Counter"),

    OAM ("", "oam", "OAM"),
    ;
    private final String doc;
    private final String var;
    private final String display;

    DmaVarName(String doc, String var, String display) {
        this.doc = doc;
        this.var = var;
        this.display = display;
    }

    /**
     * @return documentation specific name (fall back to enum name)
     */
    public String doc() {
        return "DMA." + (doc.isEmpty() ? name() : doc);
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
