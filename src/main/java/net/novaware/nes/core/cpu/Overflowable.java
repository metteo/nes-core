package net.novaware.nes.core.cpu;

public interface Overflowable {

    void setOverflow(boolean high);

    /**
     * __
     * SO negative-edge line
     */
    default void so(boolean high) {
        setOverflow(high);
    }
}
