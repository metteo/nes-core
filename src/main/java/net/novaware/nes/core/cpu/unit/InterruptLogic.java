package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;

public class InterruptLogic implements Unit {

    /*
        System Vectors:
        $FFFA, $FFFB ... NMI (Non-Maskable Interrupt) vector, 16-bit (LB, HB)
        $FFFC, $FFFD ... RES (Reset) vector, 16-bit (LB, HB)
        $FFFE, $FFFF ... IRQ (Interrupt Request) vector, 16-bit (LB, HB)
     */

    @Inject
    public InterruptLogic() {

    }

    // BRK, NMI, IRQ

    public void reset() {

    }

    public void interruptRequest() {

    }

    public void nonMaskableInterrupt() {

    }
}
