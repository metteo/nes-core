package net.novaware.nes.core.cpu.inject;

public enum CpuVarName {

    // region Basic Registers
    PC ("",  "programCounter",     "Program Counter"),
    CC ("",  "cycleCounter",       "Cycle Counter"), // TODO: maybe just "cycle" or "globalCycle"
    IC ("",  "instructionCycle",   "Instruction Cycle"),

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
    // TODO: maybe name them differently?
    CS ("", "codeSegment",  "Code Segment"), // rom?
    DS ("", "dataSegment",  "Data Segment"), // additional ram?

    // endregion
    // region Memory Devices

    RAM ("", "", "Work RAM"),
    PPU ("", "", "Picture Processing Unit"),
    ACR ("", "", "APU Channel Registers"),
    DMA ("", "", "Direct Memory Access"),
    APU ("", "", "Audio Processing Unit"),
    JOY ("", "", "Joypad Input-Output"),
    ATM ("", "", "APU Test Mode"),
    TMR ("", "", "Timer Unit"),
    RNG ("", "", "Random Number Generator"), // easy

    // endregion

    BUS("", "memoryBus", "Memory Bus"),

    // region Signals

    IRQ ("", "interruptRequest", "Interrupt Request"),
    NMI ("", "nonMaskableInterrupt", "Non-Maskable Interrupt"),
    BRK ("", "forceBreak", "Force Break"),
    S0H ("", "sprite0Hit", "Sprite 0 Hit"), // TODO: consider ppu agnostic name
    RES ("", "reset", "Reset"),
    RDY ("", "ready", "Ready"),
    SOV ("SV", "setOverflow", "Set Overflow"),

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
        return "CPU." + (doc.isEmpty() ? name() : doc);
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
