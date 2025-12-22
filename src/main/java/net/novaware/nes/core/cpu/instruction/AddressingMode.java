package net.novaware.nes.core.cpu.instruction;

public enum AddressingMode {

    // region Immediate and Single Operand

    IMMEDIATE,              // #$NN (Value is the operand)
    IMPLIED,                // No operand (e.g., NOP, TAX)
    ACCUMULATOR,            // Operand is the A register (e.g., ASL A)

    // endregion
    // region Direct Memory (Non-Indexed)

    ZERO_PAGE,              // $NN
    ABSOLUTE,               // $NNNN

    // endregion
    // region Indexed Memory

    INDEXED_ZERO_PAGE_X,    // $NN,X
    INDEXED_ZERO_PAGE_Y,    // $NN,Y
    INDEXED_ABSOLUTE_X,     // $NNNN,X
    INDEXED_ABSOLUTE_Y,     // $NNNN,Y

    // endregion
    // region Indirect

    ABSOLUTE_INDIRECT,      // Used only by JMP ($NNNN)

    PRE_INDEXED_INDIRECT_X, // ($NN,X)
    POST_INDEXED_INDIRECT_Y,// ($NN),Y

    // endregion
    // region Control Flow

    RELATIVE                // Used only by Branch* (e.g., BEQ Label)

    // endregion
}
