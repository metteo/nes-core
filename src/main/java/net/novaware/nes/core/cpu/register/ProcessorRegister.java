package net.novaware.nes.core.cpu.register;

import net.novaware.nes.core.register.Register;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;

public class ProcessorRegister extends Register {

    private boolean negative;   // 7
    private boolean overflow;   // 6
                                // 5 - always one
    private boolean b;          // 4 - 0 if pushed by irq/nmi, 1 if pushed by brk / php

    private boolean decimal;    // 3
    private boolean interrupt;  // 2 - 0 if irq enabled, 1 if irq disabled, TODO: transient!
    private boolean zero;       // 1
    private boolean carry;      // 0

    public ProcessorRegister(String name) {
        super(name);
    }

    public void powerOn() {
        negative = false;
        overflow = false;
        b = false;

        decimal = false;
        interrupt = true;
        zero = false;
        carry = false;
    }

    public void reset() {
        interrupt = true;
    }

    public boolean isNegative() {
        return negative;
    }

    public ProcessorRegister setNegative(boolean negative) {
        this.negative = negative;

        return this;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public ProcessorRegister setOverflow(boolean overflow) {
        this.overflow = overflow;

        return this;
    }

    public boolean getB() { // TODO: come up with a name
        return b;
    }

    public ProcessorRegister setB(boolean b) {
        this.b = b;

        return this;
    }

    public boolean isDecimal() {
        return decimal;
    }

    public ProcessorRegister setDecimal(boolean decimal) {
        this.decimal = decimal;

        return this;
    }

    public boolean isIrqDisabled() {
        return interrupt;
    }

    public ProcessorRegister setIrqDisabled(boolean interrupt) {
        this.interrupt = interrupt;

        return this;
    }

    public boolean isZero() {
        return zero;
    }

    public ProcessorRegister setZero(boolean zero) {
        this.zero = zero;

        return this;
    }

    public boolean getCarry() {
        return carry;
    }

    public ProcessorRegister setCarry(boolean carry) {
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
        int b = this.b ? 0x10 : 0;

        int dec = decimal ? 0x8 : 0;
        int irqd = interrupt ? 0x4 : 0;
        int zero = this.zero ? 0x2 : 0;
        int carry = this.carry ? 0x1 : 0;

        return neg | ov | one | b | dec | irqd | zero | carry;
    }
}
