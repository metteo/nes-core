package net.novaware.nes.core.memory;

import net.novaware.nes.core.util.Nameable;

public abstract class AbstractAddressable implements Nameable, Addressable {

    private final String name;

    public AbstractAddressable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
