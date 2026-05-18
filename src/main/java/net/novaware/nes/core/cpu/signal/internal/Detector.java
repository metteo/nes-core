package net.novaware.nes.core.cpu.signal.internal;

import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.util.Nameable;

public interface Detector extends Nameable { // TODO: move to pin or top level signal package

    boolean isActive();

    void set(Signal state);
}
