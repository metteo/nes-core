package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Initializable;
import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.Resettable;

import java.util.List;

public class RegisterFile implements Nameable, Initializable, Resettable {

    private final String name;

    protected List<ShortRegister> shortRegisters = List.of();
    protected List<ByteRegister> byteRegisters = List.of();
    protected List<BooleanRegister> booleanRegisters = List.of();
    // TODO: add segment registers
    // TODO: add other registers (status, EFlags etc)?

    protected RegisterFile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ShortRegister> getShortRegisters() {
        return shortRegisters;
    }

    public List<ByteRegister> getByteRegisters() {
        return byteRegisters;
    }

    public List<BooleanRegister> getBooleanRegisters() {
        return booleanRegisters;
    }

}
