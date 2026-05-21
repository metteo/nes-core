package net.novaware.nes.core.cpu.signal.internal;

import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.util.Nameable;

public interface Detector extends Nameable { // TODO: move to pin package

    boolean isActive();

    void set(Signal state);
}
