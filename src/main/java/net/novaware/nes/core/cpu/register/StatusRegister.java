package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.register.Register;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;

// TODO: consider setters that accept an int value to prevent branching?
public class StatusRegister extends Register {

    private boolean negative;   // 7
    private boolean overflow;   // 6
                                // 5 - always one
    private boolean brk;        // 4 - 0 if pushed by irq/nmi, 1 if pushed by brk / php TODO: transient!

    private boolean decimal;    // 3
    private boolean irq_off;    // 2 - 0 if irq enabled, 1 if irq disabled
    private boolean zero;       // 1
    private boolean carry;      // 0

    public StatusRegister(String name) {
        super(name);
    }

    public void initialize() {
        negative = false;
        overflow = false;
        brk = false;

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

    public boolean getBreak() {
        return brk;
    }

    public StatusRegister setBreak(boolean brk) {
        this.brk = brk;

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

    public @Unsigned byte get() {
        return ubyte(getAsInt());
    }

    public int getAsInt() {
        int neg = negative ? 0x80 : 0;
        int ov = overflow ? 0x40 : 0;
        int one = 0x20;
        int b = this.brk ? 0x10 : 0;

        int dec = decimal ? 0x8 : 0;
        int irqd = irq_off ? 0x4 : 0;
        int zero = this.zero ? 0x2 : 0;
        int carry = this.carry ? 0x1 : 0;

        return neg | ov | one | b | dec | irqd | zero | carry;
    }
}
