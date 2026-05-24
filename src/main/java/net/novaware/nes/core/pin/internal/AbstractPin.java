package net.novaware.nes.core.pin.internal;

import net.novaware.nes.core.cpu.signal.internal.Detector;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.util.BooleanConsumer;

public abstract class AbstractPin implements Pin { // TODO: AbstractInputPin

    private final String name;
    protected final Detector detector;
    protected final BooleanConsumer consumer;

    public AbstractPin(String name, Detector detector, BooleanConsumer consumer) {
        this.name = name;
        this.detector = detector;
        this.consumer = consumer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
