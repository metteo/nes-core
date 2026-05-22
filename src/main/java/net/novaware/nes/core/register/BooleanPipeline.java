package net.novaware.nes.core.register;

import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.Asserts.assertState;

public class BooleanPipeline implements Pipeline {

    private final String name;

    private boolean value;

    private boolean future;
    private int delay = -1;

    public BooleanPipeline(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    public void setDelayed(boolean future, int delay) {
        assertState(this.delay < 0, "already delaying a value"); // TODO: if it happens, allow real pipeline of values
        assertArgument(delay > 0, "delay must be positive");

        this.future = future;
        this.delay = delay;
    }

    public void cycle() {
        if (delay > 0) {
            delay--;
        }

        if (delay == 0) {
            value = future;
            delay = -1;
        }
    }

    @Override
    public String toString() {
        return name + ": " + (value ? 1 : 0);
    }
}
