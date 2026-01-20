package net.novaware.nes.core.file

import net.novaware.nes.core.test.TestDataBuilder;
import net.novaware.nes.core.util.QuantityBuilder

import static net.novaware.nes.core.util.QuantityBuilder.banks8kb;

class ProgramMemoryBuilder implements TestDataBuilder<NesMeta.ProgramMemory> {

    private NesMeta.Kind kind;
    private QuantityBuilder quantity;

    static ProgramMemoryBuilder none() {
        return new ProgramMemoryBuilder()
                .kind(NesMeta.Kind.NONE)
                .quantity(banks8kb(0));

    }

    static ProgramMemoryBuilder battery8kb(int amount) {
        return new ProgramMemoryBuilder()
                .kind(NesMeta.Kind.PERSISTENT)
                .quantity(banks8kb(amount));
    }

    static ProgramMemoryBuilder volatile8kb(int amount) {
        return new ProgramMemoryBuilder()
                .kind(NesMeta.Kind.VOLATILE)
                .quantity(banks8kb(amount));

    }

    ProgramMemoryBuilder kind(NesMeta.Kind kind) {
        this.kind = kind;
        return this;
    }

    ProgramMemoryBuilder quantity(QuantityBuilder quantity) {
        this.quantity = quantity;
        return this;
    }

    @Override
    NesMeta.ProgramMemory build() {
        return new NesMeta.ProgramMemory(kind, quantity.build());
    }
}
