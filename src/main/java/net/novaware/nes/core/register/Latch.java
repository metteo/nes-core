package net.novaware.nes.core.register;

import net.novaware.nes.core.util.Nameable;
import net.novaware.nes.core.util.Resettable;

public abstract class Latch implements Nameable, Resettable {

    private final String name;

    protected Latch(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
