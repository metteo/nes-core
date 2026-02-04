package net.novaware.nes.core.cpu.instruction;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class InstructionRegistry {

    // Static lookup map for fast retrieval by opcode (essential for performance)
    private static final Instruction[] OPCODE_MAP = new Instruction[256];

    static {
        for (Instruction instruction : Instruction.values()) {
            OPCODE_MAP[uint(instruction.opcode())] = instruction;
        }

        for(int i = 0; i < OPCODE_MAP.length; i++) {
            if(OPCODE_MAP[i] == null) {
                OPCODE_MAP[i] = Instruction.OxUK;
            }
        }
    }

    public static Instruction fromOpcode(@Unsigned byte opcode) {
        return OPCODE_MAP[uint(opcode)];
    }
}
