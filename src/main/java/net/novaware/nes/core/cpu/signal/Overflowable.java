package net.novaware.nes.core.cpu.signal;

public interface Overflowable {

    void setOverflow(Signal s);

    /**
     * __
     * SO negative-edge line
     */
    default void so(Signal s) {
        setOverflow(s);
    }
}
