package net.novaware.nes.core.cpu.instruction;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.instruction.AddressingMode.*;
import static net.novaware.nes.core.cpu.instruction.InstructionGroup.*;
import static net.novaware.nes.core.util.UnsignedTypes.ubyte;

public enum Instruction {

    // region Row 0

    Ox00(FORCE_BREAK,            IMPLIED,                0x00, 1),
    Ox01(BITWISE_OR,             PRE_INDEXED_INDIRECT_X, 0x01, 2),

    Ox05(BITWISE_OR,             ZERO_PAGE,              0x05, 2),
    Ox06(SHIFT_LEFT,             ZERO_PAGE,              0x06, 2),

    Ox08(PUSH_STATUS_TO_SP,      IMPLIED,                0x08, 1),
    Ox09(BITWISE_OR,             IMMEDIATE,              0x09, 2),
    Ox0A(SHIFT_LEFT,             ACCUMULATOR,            0x0A, 1),

    Ox0D(BITWISE_OR,             ABSOLUTE,               0x0D, 3),
    Ox0E(SHIFT_LEFT,             ABSOLUTE,               0x0E, 3),

    // endregion
    // region Row 1

    Ox10(BRANCH_IF_NEGATIVE_CLR, RELATIVE,               0x10, 2),
    Ox11(BITWISE_OR,             POST_INDEXED_INDIRECT_Y,0x11, 2),

    Ox15(BITWISE_OR,             INDEXED_ZERO_PAGE_X,    0x15, 2),
    Ox16(SHIFT_LEFT,             INDEXED_ZERO_PAGE_X,    0x16, 2),

    Ox18(CLR_CARRY,              IMPLIED,                0x18, 1),
    Ox19(BITWISE_OR,             INDEXED_ABSOLUTE_Y,     0x19, 3),

    Ox1D(BITWISE_OR,             INDEXED_ABSOLUTE_X,     0x1D, 3),
    Ox1E(SHIFT_LEFT,             INDEXED_ABSOLUTE_X,     0x1E, 3),

    // endregion
    // region Row 2

    Ox20(JUMP_TO_SUBROUTINE,     ABSOLUTE,               0x20, 3),
    Ox21(BITWISE_AND,            PRE_INDEXED_INDIRECT_X, 0x21, 2),

    Ox24(BIT_TEST,               ZERO_PAGE,              0x24, 2),
    Ox25(BITWISE_AND,            ZERO_PAGE,              0x25, 2),
    Ox26(ROTATE_LEFT,            ZERO_PAGE,              0x26, 2),

    Ox28(PULL_STATUS_FROM_SP,    IMPLIED,                0x28, 1),
    Ox29(BITWISE_AND,            IMMEDIATE,              0x29, 2),
    Ox2A(ROTATE_LEFT,            ACCUMULATOR,            0x2A, 1),

    Ox2C(BIT_TEST,               ABSOLUTE,               0x2C, 3),
    Ox2D(BITWISE_AND,            ABSOLUTE,               0x2D, 3),
    Ox2E(ROTATE_LEFT,            ABSOLUTE,               0x2E, 3),

    // endregion
    // region Row 3

    Ox30(BRANCH_IF_NEGATIVE_SET, RELATIVE,               0x30, 2),
    Ox31(BITWISE_AND,            POST_INDEXED_INDIRECT_Y,0x31, 2),

    Ox35(BITWISE_AND,            INDEXED_ZERO_PAGE_X,    0x35, 2),
    Ox36(ROTATE_LEFT,            INDEXED_ZERO_PAGE_X,    0x36, 2),

    Ox38(SET_CARRY,              IMPLIED,                0x38, 1),
    Ox39(BITWISE_AND,            INDEXED_ABSOLUTE_Y,     0x39, 3),

    Ox3D(BITWISE_AND,            INDEXED_ABSOLUTE_X,     0x3D, 3),
    Ox3E(ROTATE_LEFT,            INDEXED_ABSOLUTE_X,     0x3E, 3),

    // endregion
    // region Row 4

    Ox40(RETURN_FROM_INTERRUPT,  IMPLIED,                0x40, 1),
    Ox41(BITWISE_XOR,            PRE_INDEXED_INDIRECT_X, 0x41, 2),

    Ox45(BITWISE_XOR,            ZERO_PAGE,              0x45, 2),
    Ox46(SHIFT_RIGHT,            ZERO_PAGE,              0x46, 2),

    Ox48(PUSH_A_TO_SP,           IMPLIED,                0x48, 1),
    Ox49(BITWISE_XOR,            IMMEDIATE,              0x49, 2),
    Ox4A(SHIFT_RIGHT,            ACCUMULATOR,            0x4A, 1),

    Ox4C(JUMP_TO_LOCATION,       ABSOLUTE,               0x4C, 3),
    Ox4D(BITWISE_XOR,            ABSOLUTE,               0x4D, 3),
    Ox4E(SHIFT_RIGHT,            ABSOLUTE,               0x4E, 3),

    // endregion
    // region Row 5

    Ox50(BRANCH_IF_OVERFLOW_CLR, RELATIVE,               0x50, 2),
    Ox51(BITWISE_XOR,            POST_INDEXED_INDIRECT_Y,0x51, 2),

    Ox55(BITWISE_XOR,            INDEXED_ZERO_PAGE_X,    0x55, 2),
    Ox56(SHIFT_RIGHT,            INDEXED_ZERO_PAGE_X,    0x56, 2),

    Ox58(CLR_INTERRUPT_DISABLE,  IMPLIED,                0x58, 1),
    Ox59(BITWISE_XOR,            INDEXED_ABSOLUTE_Y,     0x59, 3),

    Ox5D(BITWISE_XOR,            INDEXED_ABSOLUTE_X,     0x5D, 3),
    Ox5E(SHIFT_RIGHT,            INDEXED_ABSOLUTE_X,     0x5E, 3),

    // endregion
    // region Row 6

    Ox60(RETURN_FROM_SUBROUTINE, IMPLIED,                0x60, 1),
    Ox61(ADD_WITH_CARRY,         PRE_INDEXED_INDIRECT_X, 0x61, 2),

    Ox65(ADD_WITH_CARRY,         ZERO_PAGE,              0x65, 2),
    Ox66(ROTATE_RIGHT,           ZERO_PAGE,              0x66, 2),

    Ox68(PULL_A_FROM_SP,         IMPLIED,                0x68, 1),
    Ox69(ADD_WITH_CARRY,         IMMEDIATE,              0x69, 2),
    Ox6A(ROTATE_RIGHT,           ACCUMULATOR,            0x6A, 1),

    Ox6C(JUMP_TO_LOCATION,       ABSOLUTE_INDIRECT,      0x6C, 3),
    Ox6D(ADD_WITH_CARRY,         ABSOLUTE,               0x6D, 3),
    Ox6E(ROTATE_RIGHT,           ABSOLUTE,               0x6E, 3),

    // endregion
    // region Row 7

    Ox70(BRANCH_IF_OVERFLOW_SET, RELATIVE,               0x70, 2),
    Ox71(ADD_WITH_CARRY,         POST_INDEXED_INDIRECT_Y,0x71, 2),

    Ox75(ADD_WITH_CARRY,         INDEXED_ZERO_PAGE_X,    0x75, 2),
    Ox76(ROTATE_RIGHT,           INDEXED_ZERO_PAGE_X,    0x76, 2),

    Ox78(SET_INTERRUPT_DISABLE,  IMPLIED,                0x78, 1),
    Ox79(ADD_WITH_CARRY,         INDEXED_ABSOLUTE_Y,     0x79, 3),

    Ox7D(ADD_WITH_CARRY,         INDEXED_ABSOLUTE_X,     0x7D, 3),
    Ox7E(ROTATE_RIGHT,           INDEXED_ABSOLUTE_X,     0x7E, 3),

    // endregion
    // region Row 8

    Ox81(STORE_A_IN_MEMORY,      PRE_INDEXED_INDIRECT_X, 0x81, 2),

    Ox84(STORE_Y_IN_MEMORY,      ZERO_PAGE,              0x84, 2),
    Ox85(STORE_A_IN_MEMORY,      ZERO_PAGE,              0x85, 2),
    Ox86(STORE_X_IN_MEMORY,      ZERO_PAGE,              0x86, 2),

    Ox88(DECREMENT_Y,            IMPLIED,                0x88, 1),

    Ox8A(TRANSFER_X_TO_A,        IMPLIED,                0x8A, 1),

    Ox8C(STORE_Y_IN_MEMORY,      ABSOLUTE,               0x8C, 3),
    Ox8D(STORE_A_IN_MEMORY,      ABSOLUTE,               0x8D, 3),
    Ox8E(STORE_X_IN_MEMORY,      ABSOLUTE,               0x8E, 3),

    // endregion
    // region Row 9

    Ox90(BRANCH_IF_CARRY_CLR,    RELATIVE,               0x90, 2),
    Ox91(STORE_A_IN_MEMORY,      POST_INDEXED_INDIRECT_Y,0x91, 2),

    Ox94(STORE_Y_IN_MEMORY,      INDEXED_ZERO_PAGE_X,    0x94, 2),
    Ox95(STORE_A_IN_MEMORY,      INDEXED_ZERO_PAGE_X,    0x95, 2),
    Ox96(STORE_X_IN_MEMORY,      INDEXED_ZERO_PAGE_Y,    0x96, 2),

    Ox98(TRANSFER_Y_TO_A,        IMPLIED,                0x98, 1),
    Ox99(STORE_A_IN_MEMORY,      INDEXED_ABSOLUTE_Y,     0x99, 3),
    Ox9A(TRANSFER_X_TO_SP,       IMPLIED,                0x9A, 1),

    Ox9D(STORE_A_IN_MEMORY,      INDEXED_ABSOLUTE_X,     0x9D, 3),

    // endregion
    // region Row A

    OxA0(LOAD_Y_WITH_MEMORY,     IMMEDIATE,              0xA0, 2),
    OxA1(LOAD_A_WITH_MEMORY,     PRE_INDEXED_INDIRECT_X, 0xA1, 2),
    OxA2(LOAD_X_WITH_MEMORY,     IMMEDIATE,              0xA2, 2),

    OxA4(LOAD_Y_WITH_MEMORY,     ZERO_PAGE,              0xA4, 2),
    OxA5(LOAD_A_WITH_MEMORY,     ZERO_PAGE,              0xA5, 2),
    OxA6(LOAD_X_WITH_MEMORY,     ZERO_PAGE,              0xA6, 2),

    OxA8(TRANSFER_A_TO_Y,        IMPLIED,                0xA8, 1),
    OxA9(LOAD_A_WITH_MEMORY,     IMMEDIATE,              0xA9, 2),
    OxAA(TRANSFER_A_TO_X,        IMPLIED,                0xAA, 1),

    OxAC(LOAD_Y_WITH_MEMORY,     ABSOLUTE,               0xAC, 3),
    OxAD(LOAD_A_WITH_MEMORY,     ABSOLUTE,               0xAD, 3),
    OxAE(LOAD_X_WITH_MEMORY,     ABSOLUTE,               0xAE, 3),

    // endregion
    // region Row B

    OxB0(BRANCH_IF_CARRY_SET,    RELATIVE,               0xB0, 2),
    OxB1(LOAD_A_WITH_MEMORY,     POST_INDEXED_INDIRECT_Y,0xB1, 2),

    OxB4(LOAD_Y_WITH_MEMORY,     INDEXED_ZERO_PAGE_X,    0xB4, 2),
    OxB5(LOAD_A_WITH_MEMORY,     INDEXED_ZERO_PAGE_X,    0xB5, 2),
    OxB6(LOAD_X_WITH_MEMORY,     INDEXED_ZERO_PAGE_Y,    0xB6, 2),

    OxB8(CLR_OVERFLOW,           IMPLIED,                0xB8, 1),
    OxB9(LOAD_A_WITH_MEMORY,     INDEXED_ABSOLUTE_Y,     0xB9, 3),
    OxBA(TRANSFER_SP_TO_X,       IMPLIED,                0xBA, 1),

    OxBC(LOAD_Y_WITH_MEMORY,     INDEXED_ABSOLUTE_X,     0xBC, 3),
    OxBD(LOAD_A_WITH_MEMORY,     INDEXED_ABSOLUTE_X,     0xBD, 3),
    OxBE(LOAD_X_WITH_MEMORY,     INDEXED_ABSOLUTE_Y,     0xBE, 3),

    // endregion
    // region Row C

    OxC0(COMPARE_Y_WITH_MEMORY,  IMMEDIATE,              0xC0, 2),
    OxC1(COMPARE_A_WITH_MEMORY,  PRE_INDEXED_INDIRECT_X, 0xC1, 2),

    OxC4(COMPARE_Y_WITH_MEMORY,  ZERO_PAGE,              0xC4, 2),
    OxC5(COMPARE_A_WITH_MEMORY,  ZERO_PAGE,              0xC5, 2),
    OxC6(DECREMENT_MEMORY,       ZERO_PAGE,              0xC6, 2),

    OxC8(INCREMENT_Y,            IMPLIED,                0xC8, 1),
    OxC9(COMPARE_A_WITH_MEMORY,  IMMEDIATE,              0xC9, 2),
    OxCA(DECREMENT_X,            IMPLIED,                0xCA, 1),

    OxCC(COMPARE_Y_WITH_MEMORY,  ABSOLUTE,               0xCC, 3),
    OxCD(COMPARE_A_WITH_MEMORY,  ABSOLUTE,               0xCD, 3),
    OxCE(DECREMENT_MEMORY,       ABSOLUTE,               0xCE, 3),

    // endregion
    // region Row D

    OxD0(BRANCH_IF_ZERO_CLR,     RELATIVE,               0xD0, 2),
    OxD1(COMPARE_A_WITH_MEMORY,  POST_INDEXED_INDIRECT_Y,0xD1, 2),

    OxD5(COMPARE_A_WITH_MEMORY,  INDEXED_ZERO_PAGE_X,    0xD5, 2),
    OxD6(DECREMENT_MEMORY,       INDEXED_ZERO_PAGE_X,    0xD6, 2),

    OxD8(CLR_DECIMAL,            IMPLIED,                0xD8, 1),
    OxD9(COMPARE_A_WITH_MEMORY,  INDEXED_ABSOLUTE_Y,     0xD9, 3),

    OxDD(COMPARE_A_WITH_MEMORY,  INDEXED_ABSOLUTE_X,     0xDD, 3),
    OxDE(DECREMENT_MEMORY,       INDEXED_ABSOLUTE_X,     0xDE, 3),

    // endregion
    // region Row E

    OxE0(COMPARE_X_WITH_MEMORY,  IMMEDIATE,              0xE0, 2),
    OxE1(SUBTRACT_WITH_BORROW,   PRE_INDEXED_INDIRECT_X, 0xE1, 2),

    OxE4(COMPARE_X_WITH_MEMORY,  ZERO_PAGE,              0xE4, 2),
    OxE5(SUBTRACT_WITH_BORROW,   ZERO_PAGE,              0xE5, 2),
    OxE6(INCREMENT_MEMORY,       ZERO_PAGE,              0xE6, 2),

    OxE8(INCREMENT_X,            IMPLIED,                0xE8, 1),
    OxE9(SUBTRACT_WITH_BORROW,   IMMEDIATE,              0xE9, 2),
    OxEA(NO_OPERATION,           IMPLIED,                0xEA, 1),

    OxEC(COMPARE_X_WITH_MEMORY,  ABSOLUTE,               0xEC, 3),
    OxED(SUBTRACT_WITH_BORROW,   ABSOLUTE,               0xED, 3),
    OxEE(INCREMENT_MEMORY,       ABSOLUTE,               0xEE, 3),

    // endregion
    // region Row F

    OxF0(BRANCH_IF_ZERO_SET,     RELATIVE,               0xF0, 2),
    OxF1(SUBTRACT_WITH_BORROW,   POST_INDEXED_INDIRECT_Y,0xF1, 2),

    OxF5(SUBTRACT_WITH_BORROW,   INDEXED_ZERO_PAGE_X,    0xF5, 2),
    OxF6(INCREMENT_MEMORY,       INDEXED_ZERO_PAGE_X,    0xF6, 2),

    OxF8(SET_DECIMAL,            IMPLIED,                0xF8, 1),
    OxF9(SUBTRACT_WITH_BORROW,   INDEXED_ABSOLUTE_Y,     0xF9, 3),

    OxFD(SUBTRACT_WITH_BORROW,   INDEXED_ABSOLUTE_X,     0xFD, 3),
    OxFE(INCREMENT_MEMORY,       INDEXED_ABSOLUTE_X,     0xFE, 3),

    // endregion

    /**
     * OxUnKnown - null object to fill the array
     */
    OxUK(InstructionGroup.UNKNOWN, AddressingMode.UNKNOWN, 0xFF, -1);

    /**
     * Simplifies enum creation without casting to byte
     */
    Instruction(InstructionGroup group, AddressingMode addressingMode, int opcode, int size) {
        this(group, addressingMode, ubyte(opcode), size);
    }

    Instruction(InstructionGroup group, AddressingMode addressingMode, @Unsigned byte opcode, int size) {
        this.group = group;
        this.addressingMode = addressingMode;
        this.opcode = opcode;
        this.size = size;
    }

    private final InstructionGroup group;
    private final AddressingMode addressingMode;
    private final @Unsigned byte opcode;
    private final int size;

    public InstructionGroup group() {
        return group;
    }

    public AddressingMode addressingMode() {
        return addressingMode;
    }

    public @Unsigned byte opcode() {
        return opcode;
    }

    /**
     * @return size of the instruction in bytes (including opcode)
     */
    public int size() {
        return size;
    }
}
