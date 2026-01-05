package net.novaware.nes.core.cpu.instruction;

public class InstructionRegistry {

    // Static lookup map for fast retrieval by opcode (essential for performance)
    private static final Instruction[] OPCODE_MAP = new Instruction[256];

    static {
        for (Instruction instruction : Instruction.values()) {
            OPCODE_MAP[instruction.opcode() & 0xFF] = instruction;
        }
    }

    // TODO: Optional is not very memory friendly in hot code?
    //@Nullable TODO: include jspecify
    public static Instruction fromOpcode(byte opcode) {
        return OPCODE_MAP[opcode & 0xFF];
    }
}
