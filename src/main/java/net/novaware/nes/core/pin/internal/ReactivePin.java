package net.novaware.nes.core.pin.internal;

import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.internal.Detector;
import net.novaware.nes.core.pin.InputPin;
import net.novaware.nes.core.util.BooleanConsumer;

public final class ReactivePin extends AbstractPin implements InputPin {

    public ReactivePin(
        String name,
        Detector detector,
        BooleanConsumer consumer
    ) {
        super(name, detector, consumer);
    }

    @Override
    public void set(Signal state) {
        detector.set(state);
        consumer.accept(detector.isActive());
    }
}
