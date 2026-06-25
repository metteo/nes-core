package net.novaware.nes.core.register;

public final class IntegerCounter extends Counter {

    private int value;

    public IntegerCounter(String name) {
        super(name);
    }

    public int getValue() {
        return value;
    } // TODO: consider shorter method names like get, set, inc, dec etc

    public void setValue(int value) {
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    /**
     * Conditional increment hopefully compiled into CMOV instruction
     * @param yes / no
     */
    public void maybeIncrement(boolean yes) {
        int amount = yes ? 1 : 0;

        value += amount;
    }

    /**
     * Conditional decrement hopefully compiled into CMOV instruction
     * @param yes / no
     */
    public void maybeDecrement(boolean yes) {
        int amount = yes ? 1 : 0;

        value -= amount;
    }

    public void reset() {
        value = 0;
    }

    public boolean isZero() {
        return value == 0;
    }

    public void maybeReset(boolean yes) {
        value = yes ? 0 : value;
    }

    public boolean isPositive() {
        return value > 0;
    }

    @Override
    public String toString() {
        return getName() + ": " + value;
    }

    public void decrementBy(int amount) {
        value -= amount;
    }

    public void incrementBy(int amount) {
        value += amount;
    }
}
