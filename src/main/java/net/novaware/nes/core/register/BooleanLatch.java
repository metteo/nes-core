package net.novaware.nes.core.register;

public class BooleanLatch extends Latch {

    @FunctionalInterface
    public interface Output {
        void set(boolean value);
    }

    private boolean empty;
    private boolean value;

    private Output output;

    public BooleanLatch(String name, Output output) {
        super(name);

        this.output = output;
        empty = true;
    }

    public void delayedSet(boolean value) {
        this.value = value;
        empty = false;
    }

    public void commit() {
        if (empty) { return; } // TODO: how to get rid of this if? always set value? but if empty it's current I?

        output.set(value);
        empty = true;
    }

    @Override
    public void reset() {
        empty = true;
    }
}
