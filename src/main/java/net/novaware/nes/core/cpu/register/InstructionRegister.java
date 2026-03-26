package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.cpu.instruction.AddressingMode;
import net.novaware.nes.core.cpu.instruction.Instruction;
import net.novaware.nes.core.cpu.instruction.InstructionGroup;
import net.novaware.nes.core.register.Register;

public class InstructionRegister extends Register {

    private InstructionGroup group = InstructionGroup.UNKNOWN;
    private AddressingMode addressing = AddressingMode.UNKNOWN;

    // TODO: consider adding fields for lambdas to decode and execute methods to get rid of switches

    public InstructionRegister(String name) {
        super(name);
    }

    public void set(Instruction instruction) {
        group = instruction.group();
        addressing = instruction.addressingMode();
    }

    public InstructionGroup getGroup() {
        return group;
    }

    public AddressingMode getAddressing() {
        return addressing;
    }

    @Override
    public String toString() {
        return getName() + ": " + group + " " + addressing;
    }
}
