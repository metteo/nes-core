package net.novaware.nes.core.cpu.signal;

public interface Interruptible {

    void interruptRequest(Signal s);
    void nonMaskableInterrupt(Signal s);
    void reset(Signal s);
    void sprite0Hit(Signal s);

    /**
     * ___
     * IRQ active-low line
     */
    default void irq(Signal s) {
        interruptRequest(s);
    }

    /**
     * ___
     * NMI negative-edge line
     */
    default void nmi(Signal s) {
        nonMaskableInterrupt(s);
    }

    /**
     * ___
     * RES active-low line
     */
    default void res(Signal s) {
        reset(s);
    }

    /**
     * ___
     * S0H active low line
     */
    default void s0h(Signal s) {
        sprite0Hit(s);
    }
}
