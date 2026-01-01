package net.novaware.nes.core.util;

import java.util.Objects;

// TODO: switch to jsr385 when they fix jpms
public class Quantity {
    private final int amount;
    private final Unit unit;

    public Quantity(int amount, Unit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public int getAmount() {
        return amount;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return amount == quantity.amount && unit == quantity.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, unit);
    }

    public enum Unit {
        BYTES
    }
}
