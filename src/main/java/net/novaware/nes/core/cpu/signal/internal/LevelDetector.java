package net.novaware.nes.core.cpu.signal.internal;

import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.pin.Pin;

import static net.novaware.nes.core.cpu.signal.Signal.not;

public class LevelDetector implements Detector, Pin {

    private final String name;
    private final Signal activeState;

    private Signal current;

    public LevelDetector(String name, Signal activeState) {
        this.name = name;
        this.activeState = activeState;
        this.current = not(activeState);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive() {
        return activeState == current;
    }

    @Override
    public void set(Signal state) {
        this.current = state;
    }
}
