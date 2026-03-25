package net.novaware.nes.core.cpu.instruction;

import static net.novaware.nes.core.cpu.instruction.AddressingCategory.DIRECT;
import static net.novaware.nes.core.cpu.instruction.AddressingCategory.INDEXED;
import static net.novaware.nes.core.cpu.instruction.AddressingCategory.INDIRECT;
import static net.novaware.nes.core.cpu.instruction.AddressingCategory.NONE;

public enum AddressingMode {

    // region Implied / Implicit

    IMPLIED                  ("",  AddressingCategory.IMPLIED, NONE, 0), // also IMPLICIT
    ACCUMULATOR              ("A", AddressingCategory.IMPLIED, NONE, 0),

    // endregion
    // region Immediate

    IMMEDIATE                ("#BYTE", AddressingCategory.IMMEDIATE, NONE, 1),

    // endregion
    // region Relative

    RELATIVE                 ("SBYTE", AddressingCategory.RELATIVE,  NONE, 1),

    // endregion
    // region Zero Page

    ZERO_PAGE                ("BYTE",      DIRECT,   NONE,    1),

    ZERO_PAGE_X              ("BYTE, X",   INDEXED,  NONE,    1),
    ZERO_PAGE_Y              ("BYTE, Y",   INDEXED,  NONE,    1),

    ZERO_PAGE_X_INDIRECT     ("(BYTE, X)", INDIRECT, INDEXED, 1),

    ZERO_PAGE_INDIRECT_Y     ("(BYTE), Y", INDIRECT, INDEXED, 1),
    ZERO_PAGE_INDIRECT_Y_R   ("(BYTE), Y", INDIRECT, INDEXED, 1),
    ZERO_PAGE_INDIRECT_Y_W   ("(BYTE), Y", INDIRECT, INDEXED, 1),

    // endregion
    // region Absolute

    ABSOLUTE          ("WORD",    DIRECT,   NONE, 2),

    ABSOLUTE_X        ("WORD, X", INDEXED,  NONE, 2), // TODO: maybe remove?
    ABSOLUTE_X_R      ("WORD, X", INDEXED,  NONE, 2),
    ABSOLUTE_X_W      ("WORD, X", INDEXED,  NONE, 2),

    ABSOLUTE_Y        ("WORD, Y", INDEXED,  NONE, 2), // TODO: maybe remove
    ABSOLUTE_Y_R      ("WORD, Y", INDEXED,  NONE, 2),
    ABSOLUTE_Y_W      ("WORD, Y", INDEXED,  NONE, 2),

    ABSOLUTE_INDIRECT ("(WORD)",  INDIRECT, NONE, 2),

    // endregion

    // NOTE: -2 to satisfy instruction / addressing mode size relation
    UNKNOWN                  ("?", NONE, NONE, -2); // NULL Object

    AddressingMode(String format, AddressingCategory primary, AddressingCategory secondary, int size) {
        this.format = format;
        this.primary = primary;
        this.secondary = secondary;
        this.size = size;
    }

    private String format;
    private AddressingCategory primary;
    private AddressingCategory secondary;

    /**
     * Number of bytes the operand takes up
     */
    private final int size;

    public int size() {
        return size;
    }
}
