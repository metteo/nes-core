package net.novaware.nes.core.cpu.instruction;

public enum InstructionCategory {

    ARITHMETIC,

    BRANCHING,

    COMPARISON,

    CONTROL_FLOW,

    FLAG_CONTROL,

    /**
     * https://www.nesdev.org/wiki/CPU_unofficial_opcodes#Games_using_unofficial_opcodes
     */
    ILLEGAL,

    INTERRUPTS,

    LOGICAL,

    MEMORY_ACCESS,

    MISCELLANEOUS,

    REGISTER_TRANSFER,

    SHIFTS_ROTATES,

    STACK_OPS,

    UNKNOWN
}
