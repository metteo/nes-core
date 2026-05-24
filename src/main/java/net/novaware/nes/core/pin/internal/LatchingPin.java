package net.novaware.nes.core.pin.internal;

import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.internal.Detector;
import net.novaware.nes.core.pin.InputPin;
import net.novaware.nes.core.util.BooleanConsumer;

public class LatchingPin extends AbstractPin implements InputPin {

    public LatchingPin(
        String name,
        Detector detector,
        BooleanConsumer consumer
    ) {
        super(name, detector, consumer);
    }

    @Override
    public void set(Signal state) {
        detector.set(state);

        if (detector.isActive()) {
            consumer.accept(true);
        }
    }
}
