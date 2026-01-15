package net.novaware.nes.core.util;

import com.google.auto.value.AutoBuilder;

// TODO: switch to jsr385 when they fix jpms
public record Quantity(int amount, Unit unit) {

    public static final Quantity ZERO_BYTES = new Quantity(0, Unit.BYTES);

    // TODO: assert that amount is positive!

    public int toBytes() {
        int amount = amount();
        return switch (unit()) {
            case BYTES -> amount;
            case BANK_512B -> amount * 512;
            case BANK_8KB -> amount * 8 * 1024;
            case BANK_16KB -> amount * 16 * 1024;
            case BANK_32KB -> amount * 32 * 1024;
        };
    }

    public enum Unit {
        BYTES,
        BANK_512B,
        BANK_8KB,
        BANK_16KB,
        BANK_32KB
    }

    @AutoBuilder
    public interface Builder {
        Builder amount(int amount);

        Builder unit(Unit unit);

        Quantity build();

    }
}
