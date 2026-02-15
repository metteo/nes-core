package net.novaware.nes.core.cpu.instruction;

public enum AddressingMode {

    // region Immediate and Single Operand

    IMMEDIATE(1),              // #$NN (Value is the operand)
    IMPLIED(0),                // No operand (e.g., NOP, TAX)
    ACCUMULATOR(0),            // Operand is the A register (e.g., ASL A)

    // endregion
    // region Direct Memory (Non-Indexed)

    ZERO_PAGE(1),              // $NN
    ABSOLUTE(2),               // $NNNN

    // endregion
    // region Indexed Memory

    INDEXED_ZERO_PAGE_X(1),    // $NN,X
    INDEXED_ZERO_PAGE_Y(1),    // $NN,Y
    INDEXED_ABSOLUTE_X(2),     // $NNNN,X
    INDEXED_ABSOLUTE_Y(2),     // $NNNN,Y

    // endregion
    // region Indirect

    ABSOLUTE_INDIRECT(2),      // Used only by JMP ($NNNN)

    PRE_INDEXED_INDIRECT_X(1), // ($NN,X)
    POST_INDEXED_INDIRECT_Y(1),// ($NN),Y

    // endregion
    // region Control Flow

    RELATIVE(1),               // Used only by Branch* (e.g., BEQ Label)

    // endregion

    // NOTE: -2 to satisfy instruction / addressing mode size relation
    UNKNOWN(-2);               // NULL Object

    AddressingMode(int size) {
        this.size = size;
    }

    /**
     * Number of bytes the address takes up
     */
    private final int size;

    public int size() {
        return size;
    }
}
