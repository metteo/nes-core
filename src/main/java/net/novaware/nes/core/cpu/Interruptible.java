package net.novaware.nes.core.cpu;

public interface Interruptible {

    void interruptRequest(boolean high);
    void nonMaskableInterrupt(boolean high);
    void reset(boolean high);

    /**
     * ___
     * IRQ active-low line
     */
    default void irq(boolean high) {
        interruptRequest(high);
    }

    /**
     * ___
     * NMI negative-edge line
     */
    default void nmi(boolean high) {
        nonMaskableInterrupt(high);
    }

    /**
     * ___
     * RST active-low line
     */
    default void rst(boolean high) {
        reset(high);
    }
}
