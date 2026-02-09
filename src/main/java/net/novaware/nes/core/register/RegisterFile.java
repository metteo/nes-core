package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Initializable;
import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.Resettable;

import java.util.List;

public class RegisterFile implements Nameable, Initializable, Resettable {

    private final String name;

    protected List<AddressRegister> addressRegisters = List.of();
    protected List<DataRegister> dataRegisters = List.of();

    protected RegisterFile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<AddressRegister> getAddressRegisters() {
        return addressRegisters;
    }

    public List<DataRegister> getDataRegisters() {
        return dataRegisters;
    }
}
