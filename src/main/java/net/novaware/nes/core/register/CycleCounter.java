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

    /**
     * Unconditional increment
     */
    public void increment() {
        value++;
    }

    /**
     * Conditional increment hopefully compiled into CMOV instruction
     * @param yes / no
     */
    public void maybeIncrement(boolean yes) {
        value += yes ? 1 : 0;
    }

    public void mark() {
        mark = value;
    }

    public long diff() {
        return value - mark;
    }
}
