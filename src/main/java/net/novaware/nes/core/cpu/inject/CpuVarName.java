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

    SP ("S", "stackPointer",       "Stack Pointer"),

    // endregion
    // region Extended Registers

    MA ("",  "memoryAddress",      "Memory Address"),
    MD ("",  "memoryData",         "Memory Data"),

    CI ("",  "currentInstruction", "Current Instruction"),
    CO ("",  "currentOperand",     "Current Operand"),

    DI ("",  "decodedInstruction", "Decoded Instruction"),
    DO ("",  "decodedOperand",     "Decoded Operand"),

    // endregion
    // region Segment Registers

    ZP ("", "zeroPage",     "Zero Page"),
    SS ("", "stackSegment", "Stack Segment"),
    CS ("", "codeSegment",  "Code Segment"),
    DS ("", "dataSegment",  "Data Segment"),

    // endregion
    // region Memory Areas

    RAM ("",  "?", "RAM"),
    PPU ("", "", ""),
    APU ("", "", ""),
    CART ("", "", ""),

    // endregion

    BUS("", "memoryBus", "Memory Bus"),

    // TODO: nmi, irq, rst, s0h

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
