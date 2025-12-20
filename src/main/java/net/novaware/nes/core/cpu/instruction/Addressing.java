package net.novaware.nes.core.cpu.instruction;

public enum Addressing {

    IMMEDIATE,

    IMPLIED, // or IMPLICIT / ACCUMULATOR

    ABSOLUTE,

    ZERO_PAGE,

    INDEXED_ABSOLUTE_X,

    INDEXED_ABSOLUTE_Y,

    INDEXED_ZERO_PAGE_X,
    INDEXED_ZERO_PAGE_Y,

    INDIRECT,

    PRE_INDEXED_INDIRECT_X,
    POST_INDEXED_INDIRECT_Y,

    RELATIVE
}
