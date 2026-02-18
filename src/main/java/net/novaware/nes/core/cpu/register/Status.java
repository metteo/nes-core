package net.novaware.nes.core.cpu.register;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.ubyte;
import static net.novaware.nes.core.util.UnsignedTypes.sint;

/**
 * NOTE: The 'к' in 'breaк' is Cyrillic (Unicode U+043A) and ensures
 *       that the project is always built using UTF-8
 */
@SuppressWarnings("NonAsciiCharacters") // only for breaк in this class, getters / setters hide it also in tests
public class Status {

    private boolean negative;     // 7 - signed mode
    private boolean overflow;     // 6 - signed mode
                                  // 5 - always one
    private boolean breaк;        // 4 - 0 if pushed by irq/nmi, 1 if pushed by brk / php

    private boolean decimal;      // 3 - ADC and SBC in decimal mode
    private boolean irq_off;      // 2 - 0 if irq enabled, 1 if irq disabled
    private boolean zero;         // 1 - zero flag
    private boolean carry;        // 0 - carry or !borrow flag

    public Status() {
        negative = false;
        overflow = false;
        // one
        breaк = false;

        decimal = false;
        irq_off = true;
        zero = false;
        carry = false;
    }

    public boolean isNegative() {
        return negative;
    }

    public Status setNegative(boolean negative) {
        this.negative = negative;

        return this;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public Status setOverflow(boolean overflow) {
        this.overflow = overflow;

        return this;
    }

    public boolean getBreak() {
        return breaк;
    }

    public Status setBreak(boolean brk) {
        this.breaк = brk;

        return this;
    }

    public boolean isDecimal() {
        return decimal;
    }

    public Status setDecimal(boolean decimal) {
        this.decimal = decimal;

        return this;
    }

    public boolean isIrqDisabled() {
        return irq_off;
    }

    public Status setIrqDisabled(boolean irqDisabled) {
        this.irq_off = irqDisabled;

        return this;
    }

    public boolean isZero() {
        return zero;
    }

    public Status setZero(boolean zero) {
        this.zero = zero;

        return this;
    }

    public boolean getCarry() {
        return carry;
    }

    public Status setCarry(boolean carry) {
        this.carry = carry;

        return this;
    }

    public boolean getBorrow() {
        return !carry;
    }

    public Status setBorrow(boolean borrow) {
        this.carry = !borrow;

        return this;
    }

    public @Unsigned byte get() {
        return ubyte(getAsInt());
    }

    public int getAsInt() {
        int neg = negative ? 0x80 : 0;
        int ov = overflow ? 0x40 : 0;
        int one = 0x20;
        int brk = breaк ? 0x10 : 0;

        int dec = decimal ? 0x8 : 0;
        int irqd = irq_off ? 0x4 : 0;
        int zero = this.zero ? 0x2 : 0;
        int carry = this.carry ? 0x1 : 0;

        return neg | ov | one | brk | dec | irqd | zero | carry;
    }

    public void set(@Unsigned byte data) {
        setAsByte(sint(data));
    }

    public void setAsByte(int data) {
        negative = (data & 0x80) != 0;
        overflow = (data & 0x40) != 0;
        // bit 5 is always one
        breaк = (data & 0x10) != 0;

        decimal = (data & 0x8) != 0;
        irq_off = (data & 0x4) != 0;
        zero = (data & 0x2) != 0;
        carry = (data & 0x1) != 0;
    }
}
