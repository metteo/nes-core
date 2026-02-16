package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.register.Register;

/**
 * Processor status register
 */
public class StatusRegister extends Register {

    private boolean negative;     // 7 - signed mode
    private boolean overflow;     // 6 - signed mode
    //              unused        // 5 - always one
    //              break         // 4 - transient

    private boolean decimal;      // 3 - ADC and SBC in decimal mode
    private boolean irq_off;      // 2 - 0 if irq enabled, 1 if irq disabled
    private boolean zero;         // 1 - zero flag
    private boolean carry;        // 0 - carry or !borrow flag

    /**
     * Copy of status register that can be manipulated during stack operations
     */
    private transient Status copy;

    public StatusRegister(String name) {
        super(name);

        copy = new Status();
    }

    public void initialize() {
        negative = false;
        overflow = false;

        decimal = false;
        irq_off = true;
        zero = false;
        carry = false;
    }

    public void reset() {
        irq_off = true;
    }

    public boolean isNegative() {
        return negative;
    }

    public StatusRegister setNegative(boolean negative) {
        this.negative = negative;

        return this;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public StatusRegister setOverflow(boolean overflow) {
        this.overflow = overflow;

        return this;
    }

    public boolean isDecimal() {
        return decimal;
    }

    public StatusRegister setDecimal(boolean decimal) {
        this.decimal = decimal;

        return this;
    }

    public boolean isIrqDisabled() {
        return irq_off;
    }

    // TODO: if the flag is changed after irq was sampled there should be 1 instruction delay
    //       verify this is correct in our pseudo pipeline (opcode fetch at the end)
    //       on the other hand, when returning from interrupt there is no delay
    public StatusRegister setIrqDisabled(boolean irqDisabled) {
        this.irq_off = irqDisabled;

        return this;
    }

    public boolean isZero() {
        return zero;
    }

    public StatusRegister setZero(boolean zero) {
        this.zero = zero;

        return this;
    }

    public boolean getCarry() {
        return carry;
    }

    public StatusRegister setCarry(boolean carry) {
        this.carry = carry;

        return this;
    }

    public boolean getBorrow() {
        return !carry;
    }

    public StatusRegister setBorrow(boolean borrow) {
        this.carry = !borrow;

        return this;
    }

    public Status get() {
        copy.setNegative(negative)
                .setOverflow(overflow)
                .setBreak(false) // default
                .setDecimal(decimal)
                .setIrqDisabled(irq_off)
                .setZero(zero)
                .setCarry(carry);

        return copy;
    }

    public void set(Status status) {
        negative = status.isNegative();
        overflow = status.isOverflow();
        decimal = status.isDecimal();
        setIrqDisabled(status.isIrqDisabled());
        zero = status.isZero();
        carry = status.getCarry();
    }
}
