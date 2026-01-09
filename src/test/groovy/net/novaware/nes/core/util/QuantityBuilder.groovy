package net.novaware.nes.core.util;

import net.novaware.nes.core.file.TestDataBuilder;

class QuantityBuilder extends AutoBuilder_Quantity_Builder implements TestDataBuilder<Quantity> {

    QuantityBuilder() {
    }

    QuantityBuilder(Quantity source) {
        super(source);
    }

    static QuantityBuilder quantity() {
        return new QuantityBuilder();
    }

    static QuantityBuilder bytes(int amount) {
        return (QuantityBuilder) quantity()
                .amount(amount)
                .unit(Quantity.Unit.BYTES);
    }

    static QuantityBuilder banks16kb(int amount) {
        return (QuantityBuilder) quantity()
                .amount(amount)
                .unit(Quantity.Unit.BANK_16KB);
    }

    static QuantityBuilder banks8kb(int amount) {
        return (QuantityBuilder) quantity()
                .amount(amount)
                .unit(Quantity.Unit.BANK_8KB);
    }

    static QuantityBuilder banks512b(int amount) {
        return (QuantityBuilder) quantity()
                .amount(amount)
                .unit(Quantity.Unit.BANK_512B);
    }
}
