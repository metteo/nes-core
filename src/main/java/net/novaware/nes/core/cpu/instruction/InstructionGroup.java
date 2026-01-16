package net.novaware.nes.core.cpu.instruction;

import java.util.List;

import static java.util.Comparator.comparing;
import static net.novaware.nes.core.cpu.instruction.InstructionCategory.*;

public enum InstructionGroup {

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
    // region Branching

    BRANCH_IF_EQUAL         ("BEQ", BRANCHING),
    BRANCH_IF_NOT_EQUAL     ("BNE", BRANCHING),

    BRANCH_IF_CARRY_SET     ("BCS", BRANCHING),
    BRANCH_IF_CARRY_CLR     ("BCC", BRANCHING),

    BRANCH_IF_PLUS          ("BPL", BRANCHING),
    BRANCH_IF_MINUS         ("BMI", BRANCHING),

    BRANCH_IF_OVERFLOW_SET  ("BVS", BRANCHING),
    BRANCH_IF_OVERFLOW_CLR  ("BVC", BRANCHING),

    // endregion
    // region Comparison

    COMPARE_A               ("CMP", COMPARISON),
    COMPARE_X               ("CPX", COMPARISON),
    COMPARE_Y               ("CPY", COMPARISON),

    // endregion
    // region Control flow

    JUMP_TO                 ("JMP", CONTROL_FLOW),

    JUMP_TO_SUBROUTINE      ("JSR", CONTROL_FLOW),
    RETURN_FROM_SUBROUTINE  ("RTS", CONTROL_FLOW),

    // endregion
    // region Flag control

    SET_CARRY               ("SEC", FLAG_CONTROL),
    CLR_CARRY               ("CLC", FLAG_CONTROL),

    SET_DECIMAL             ("SED", FLAG_CONTROL), // TODO: not available on nes?
    CLR_DECIMAL             ("CLD", FLAG_CONTROL), // TODO: not available on nes?

    SET_INTERRUPT_DISABLE   ("SEI", FLAG_CONTROL),
    CLR_INTERRUPT_DISABLE   ("CLI", FLAG_CONTROL),

    CLR_OVERFLOW            ("CLV", FLAG_CONTROL),

    // endregion
    // region Interrupts

    BREAK                   ("BRK", INTERRUPTS), // software IRQ,
    RETURN_FROM_INTERRUPT   ("RTI", INTERRUPTS),

    // endregion
    // region Logical

    BITWISE_AND             ("AND", LOGICAL),
    BITWISE_OR              ("ORA", LOGICAL),
    BITWISE_XOR             ("EOR", LOGICAL),
    BIT_TEST                ("BIT", LOGICAL),

    // endregion
    // region Memory Access

    LOAD_A_WITH_MEMORY      ("LDA", MEMORY_ACCESS),
    STORE_A_IN_MEMORY       ("STA", MEMORY_ACCESS),

    LOAD_X_WITH_MEMORY      ("LDX", MEMORY_ACCESS),
    STORE_X_IN_MEMORY       ("STX", MEMORY_ACCESS),

    LOAD_Y_WITH_MEMORY      ("LDY", MEMORY_ACCESS),
    STORE_Y_IN_MEMORY       ("STY", MEMORY_ACCESS),

    // endregion
    // region Misc

    NO_OPERATION            ("NOP", MISCELLANEOUS),

    // endregion
    // region Register transfer

    TRANSFER_A_TO_X         ("TAX", REGISTER_TRANSFER),
    TRANSFER_X_TO_A         ("TXA", REGISTER_TRANSFER),

    TRANSFER_A_TO_Y         ("TAY", REGISTER_TRANSFER),
    TRANSFER_Y_TO_A         ("TYA", REGISTER_TRANSFER),

    // endregion
    // region Shifts & Rotates

    ARITHMETIC_SHIFT_LEFT   ("ASL", SHIFTS_ROTATES),
    LOGICAL_SHIFT_RIGHT     ("LSR", SHIFTS_ROTATES),

    ROTATE_LEFT             ("ROL", SHIFTS_ROTATES),
    ROTATE_RIGHT            ("ROR", SHIFTS_ROTATES),

    // endregion
    // region Stack Operations

    PUSH_A                  ("PHA", STACK_OPS),
    PULL_A                  ("PLA", STACK_OPS),

    PUSH_PROC_STATUS_TO_SP  ("PHP", STACK_OPS),
    PULL_PROC_STATUS_FROM_SP("PLP", STACK_OPS),

    TRANSFER_SP_TO_X        ("TSX", STACK_OPS),
    TRANSFER_X_TO_SP        ("TXS", STACK_OPS);

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
                .forEach(s -> System.out.println(s)); // NOTE: lambda will cause checker "Incompatible receiver type"
    }

    private static String toMnemonicWithDescription(InstructionGroup ig) {
        return ig.mnemonic() + " - " + ig.name().replace("_", " ");
    }
}
