package net.novaware.nes.core.cpu.signal.internal;

import net.novaware.nes.core.cpu.signal.Signal;

import static net.novaware.nes.core.cpu.signal.Signal.not;

public class EdgeDetector implements Detector {

    private final String name;
    private final Signal activeState;

    private Signal previous;
    private Signal current;

    public EdgeDetector(String name, Signal activeState) {
        this.name = name;
        this.activeState = activeState;

        current = not(activeState);
        previous = current;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive() {
        return current == activeState && previous != current;
    }

    @Override
    public void set(Signal s) {
        previous = current;
        current = s;
    }
}
