package net.novaware.nes.core.register;

public final class BooleanRegister extends Register {

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

    public void maybeToggle(boolean yes) {
        value = yes ? !value : value;
    }

    @Override
    public String toString() {
        return getName() + ": " + get();
    }
}
