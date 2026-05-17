package net.novaware.nes.core.clock;

public interface ClockReceiver {

    /**
     * @return actual clock cycles consumed
     */
    int cycle();
}
