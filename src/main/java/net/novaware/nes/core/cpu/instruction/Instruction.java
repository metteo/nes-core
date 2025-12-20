package net.novaware.nes.core.cpu.instruction;

import static net.novaware.nes.core.cpu.instruction.Addressing.*;
import static net.novaware.nes.core.cpu.instruction.InstructionGroup.*;

public enum Instruction {

    Ox00(BREAK,             IMPLIED,    0x00, 1),

    Ox65(ADD_WITH_CARRY,    ZERO_PAGE,  0x65, 2),
    Ox69(ADD_WITH_CARRY,    IMMEDIATE,  0x69, 2);

    // TODO: add the rest

    /**
     * Simplifies enum creation without casting to byte
     */
    Instruction(InstructionGroup group, Addressing addressing, int opcode, int size) {
        this(group, addressing, (byte) opcode, size);
    }

    Instruction(InstructionGroup group, Addressing addressing, byte opcode, int size) {
        this.group = group;
        this.addressing = addressing;
        this.opcode = opcode;
        this.size = size;
    }

    private final InstructionGroup group;
    private final Addressing addressing;
    private final byte opcode;
    private final int size;

    public InstructionGroup group() {
        return group;
    }

    public Addressing addressing() {
        return addressing;
    }

    public byte opcode() {
        return opcode;
    }

    /**
     * @return size of the instruction in bytes (including opcode)
     */
    public int size() {
        return size;
    }
}
