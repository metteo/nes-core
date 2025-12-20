package net.novaware.nes.core.cpu.instruction;

import java.util.List;

import static java.util.Comparator.comparing;
import static net.novaware.nes.core.cpu.instruction.InstructionCategory.*;

public enum InstructionGroup {

    // region Access

    LOAD_A_WITH_MEMORY      ("LDA", ACCESS),
    STORE_A_IN_MEMORY       ("STA", ACCESS),

    LOAD_X_WITH_MEMORY      ("LDX", ACCESS),
    STORE_X_IN_MEMORY       ("STX", ACCESS),

    LOAD_Y_WITH_MEMORY      ("LDY", ACCESS),
    STORE_Y_IN_MEMORY       ("STY", ACCESS),

    // endregion
    // region Arithmetic

    ADD_WITH_CARRY          ("ADC", ARITHMETIC),
    SUBTRACT_WITH_CARRY     ("SBC", ARITHMETIC),

    INCREMENT_MEMORY        ("INC", ARITHMETIC),
    DECREMENT_MEMORY        ("DEC", ARITHMETIC),

    INCREMENT_X             ("INX", ARITHMETIC),
    DECREMENT_X             ("DEX", ARITHMETIC),

    INCREMENT_Y             ("INY", ARITHMETIC),
    DECREMENT_Y             ("DEY", ARITHMETIC),

    // endregion
    // region Bitwise

    BITWISE_AND             ("AND", BITWISE),
    BITWISE_OR              ("ORA", BITWISE),
    BITWISE_XOR             ("EOR", BITWISE),
    BIT_TEST                ("BIT", BITWISE),

    // endregion
    // region Branch

    BRANCH_IF_EQUAL         ("BEQ", BRANCH),
    BRANCH_IF_NOT_EQUAL     ("BNE", BRANCH),

    BRANCH_IF_CARRY_SET     ("BCS", BRANCH),
    BRANCH_IF_CARRY_CLR     ("BCC", BRANCH),

    BRANCH_IF_PLUS          ("BPL", BRANCH),
    BRANCH_IF_MINUS         ("BMI", BRANCH),

    BRANCH_IF_OVERFLOW_SET  ("BVS", BRANCH),
    BRANCH_IF_OVERFLOW_CLR  ("BVC", BRANCH),

    // endregion
    // region Compare

    COMPARE_A               ("CMP", COMPARE),
    COMPARE_X               ("CPX", COMPARE),
    COMPARE_Y               ("CPY", COMPARE),

    // endregion
    // region Flags

    SET_CARRY               ("SEC", FLAGS),
    CLR_CARRY               ("CLC", FLAGS),

    SET_DECIMAL             ("SED", FLAGS),
    CLR_DECIMAL             ("CLD", FLAGS),

    SET_INTERRUPT_DISABLE   ("SEI", FLAGS),
    CLR_INTERRUPT_DISABLE   ("CLI", FLAGS),

    CLR_OVERFLOW            ("CLV", FLAGS),

    // endregion
    // region Interrupt

    BREAK                   ("BRK", INTERRUPT), // software IRQ,
    RETURN_FROM_INTERRUPT   ("RTI", INTERRUPT),

    // endregion
    // region Jump

    JUMP_TO                 ("JMP", JUMP),

    JUMP_TO_SUBROUTINE      ("JSR", JUMP),
    RETURN_FROM_SUBROUTINE  ("RTS", JUMP),

    // endregion
    // region Other

    NO_OPERATION            ("NOP", OTHER),

    // endregion
    // region Shift

    ARITHMETIC_SHIFT_LEFT   ("ASL", SHIFT),
    LOGICAL_SHIFT_RIGHT     ("LSR", SHIFT),

    ROTATE_LEFT             ("ROL", SHIFT),
    ROTATE_RIGHT            ("ROR", SHIFT),

    // endregion
    // region Stack

    PUSH_A                  ("PHA", STACK),
    PULL_A                  ("PLA", STACK),

    PUSH_PROC_STATUS_TO_SP  ("PHP", STACK),
    PULL_PROC_STATUS_FROM_SP("PLP", STACK),

    TRANSFER_SP_TO_X        ("TSX", STACK),
    TRANSFER_X_TO_SP        ("TXS", STACK),

    // endregion
    // region Transfer

    TRANSFER_A_TO_X         ("TAX", TRANSFER),
    TRANSFER_X_TO_A         ("TXA", TRANSFER),

    TRANSFER_A_TO_Y         ("TAY", TRANSFER),
    TRANSFER_Y_TO_A         ("TYA", TRANSFER);

    // endregion

    private static final List<InstructionGroup> instances = List.of(values());

    private final String mnemonic;
    private final InstructionCategory category;

    InstructionGroup(String mnemonic, InstructionCategory category) {
        this.category = category;
        this.mnemonic = mnemonic;
    }

    public InstructionCategory category() {
        return category;
    }

    public String mnemonic() {
        return mnemonic;
    }

    public static List<InstructionGroup> instances() {
        return instances;
    }

    /**
     * Utility to print mnemonics with description to console
     */
    static void main() {
        instances().stream()
                .sorted(comparing(InstructionGroup::mnemonic))
                .map(InstructionGroup::toMnemonicWithDescription)
                .forEach(System.out::println);
    }

    private static String toMnemonicWithDescription(InstructionGroup ig) {
        return ig.mnemonic() + " - " + ig.name().replace("_", " ");
    }
}
