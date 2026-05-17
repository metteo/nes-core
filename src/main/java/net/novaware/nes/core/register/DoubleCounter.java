package net.novaware.nes.core.register;

public class DoubleCounter extends Counter {

    private double value;

    public DoubleCounter(String name) {
        super(name);
    }

    public double getValue() {
        return value;
    } // TODO: consider shorter method names like get, set, inc, dec etc

    public void setValue(double value) {
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public void decrementBy(double amount) {
        value -= amount;
    }

    public void reset() {
        value = 0d;
    }

    public boolean isPositive() {
        return value > 0;
    }

    @Override
    public String toString() {
        return getName() + ": " + value;
    }
}
