package net.novaware.nes.core.cpu.instruction;

import org.jspecify.annotations.Nullable;

public class InstructionRegistry {

    // Static lookup map for fast retrieval by opcode (essential for performance)
    private static final Instruction[] OPCODE_MAP = new Instruction[256];

    static {
        for (Instruction instruction : Instruction.values()) {
            OPCODE_MAP[instruction.opcode() & 0xFF] = instruction;
        }
    }

    public static @Nullable Instruction fromOpcode(byte opcode) {
        return OPCODE_MAP[opcode & 0xFF];
    }
}
