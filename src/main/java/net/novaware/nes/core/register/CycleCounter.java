package net.novaware.nes.core.register;

public class CycleCounter extends Register { // TODO: test

    private long value;
    private int subValue; // current instruction / current scan line

    private long mark;

    public CycleCounter(String name) {
        super(name);
    }

    public long getValue() {
        return value;
    }

    public int getSubValue() {
        return subValue;
    }

    public void setValue(long value) {
        this.value = value;
    }

    /**
     * Unconditional increment
     */
    public void increment() {
        value++;
        subValue++;
    }

    /**
     * Conditional increment hopefully compiled into CMOV instruction
     * @param yes / no
     */
    public void maybeIncrement(boolean yes) {
        int amount = yes ? 1 : 0;

        value += amount;
        subValue += amount;
    }

    public void reset() {
        value = 0;
        subValue = 0;
    }

    public void subReset() {
        subValue = 0;
    }

    public void mark() {
        mark = value;
    }

    public long diff() {
        return value - mark;
    }

    @Override
    public String toString() {
        return getName() + ": " + value;
    }
}
