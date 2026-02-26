package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.register.Register;

public class EFlags extends Register {

    private boolean parity;

    private boolean adjust; // carry for BCD

    private boolean trap; // ready line enables stepping

    private boolean decimalDisable;

    private boolean spriteInterrupt;

    private boolean two; // reserved

    private boolean three; // reserved

    private boolean four; // reserved

    protected EFlags(String name) {
        super(name);
    }
}
