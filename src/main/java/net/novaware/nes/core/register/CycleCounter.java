package net.novaware.nes.core.register;

public class CycleCounter extends Register {

    private long value;
    private long mark;

    public CycleCounter(String name) {
        super(name);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public void mark() {
        mark = value;
    }

    public long diff() {
        return value - mark;
    }
}
