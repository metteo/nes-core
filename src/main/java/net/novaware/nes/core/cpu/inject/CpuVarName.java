package net.novaware.nes.core.cpu.inject;

public enum CpuVarName {

    // region Basic Registers
    PC ("",  "programCounter",     "Program Counter"),
    CC ("",  "cycleCounter",       "Cycle Counter"),

    A  ("",  "accumulator",        "Accumulator"),
    X  ("",  "indexX",             "Index X"),
    Y  ("",  "indexY",             "Index Y"),

    PS ("P", "processorStatus",    "Processor Status"),
    ES ("",  "extendedStatus",     "Extended Status"),
    ID ("",  "interruptDisabled",  "Interrupt Disabled"),

    SP ("S", "stackPointer",       "Stack Pointer"),

    // endregion
    // region Extended Registers

    MA ("",  "memoryAddress",      "Memory Address"),
    MD ("",  "memoryData",         "Memory Data"),

    PA ("",  "prefetchAddress",    "Prefetch Address"),
    CI ("",  "currentInstruction", "Current Instruction"),
    CO ("",  "currentOperand",     "Current Operand"),

    DI ("",  "decodedInstruction", "Decoded Instruction"),
    DO ("",  "decodedOperand",     "Decoded Operand"),

    // endregion
    // region Segment Registers

    ZP ("", "zeroPage",     "Zero Page"),
    SS ("", "stackSegment", "Stack Segment"),
    OS ("", "oamSegment",   "OAM Segment"),
    CS ("", "codeSegment",  "Code Segment"),
    DS ("", "dataSegment",  "Data Segment"),

    // endregion
    // region Memory Areas

    RAM ("",  "?", "RAM"),
    PPU ("", "", "PPU"),
    APU ("", "", "APU"),
    IO  ("", "", "IO"),
    FDS ("", "", "FDS"),
    CART ("", "", "Cartridge"),

    // endregion

    BUS("", "memoryBus", "Memory Bus"),

    // region Signals

    IRQ ("", "interruptRequest", "Interrupt Request"),
    NMI ("", "nonMaskableInterrupt", "Non-Maskable Interrupt"),
    BRK ("", "forceBreak", "Force Break"),
    S0H ("", "sprite0Hit", "Sprite 0 Hit"), // TODO: consider ppu agnostic name
    RES ("", "reset", "Reset"),
    RDY ("", "ready", "Ready"),
    SOV ("SO", "setOverflow", "Set Overflow"),

    // endregion
    ;
    private final String doc;
    private final String var;
    private final String display;

    CpuVarName(String doc, String var, String display) {
        this.doc = doc;
        this.var = var;
        this.display = display;
    }

    /**
     * @return documentation specific name (fall back to enum name)
     */
    public String doc() {
        return doc.isEmpty() ? name() : doc;
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
