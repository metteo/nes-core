package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Nameable;

public abstract class Register implements Nameable {

    private final String name; // TODO: consider switching to CpuVarName, but what about a PPU / APU? Common interface?

    protected Register(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
