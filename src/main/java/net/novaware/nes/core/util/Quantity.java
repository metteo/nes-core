package net.novaware.nes.core.util;

// TODO: switch to jsr385 when they fix jpms
public record Quantity(int amount, Unit unit) {

    public static final Quantity ZERO_BYTES = new Quantity(0, Unit.BYTES);

    public enum Unit {
        BYTES
    }
}
