package net.novaware.nes.core.register;

public class BooleanRegister extends Register {

    private boolean value;

    public BooleanRegister(String name) {
        super(name);
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    public void toggle() {
        value = !value;
    }

    @Override
    public String toString() {
        return getName() + ": " + get();
    }
}
