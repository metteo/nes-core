package net.novaware.nes.core.pin;

import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.util.Nameable;

public interface Pin extends Nameable {

    void set(Signal state);
}
