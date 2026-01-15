package net.novaware.nes.core.file

import net.novaware.nes.core.test.TestDataBuilder;
import net.novaware.nes.core.util.QuantityBuilder;

class ProgramMemoryBuilder implements TestDataBuilder<NesMeta.ProgramMemory> {

    private NesMeta.Kind kind;
    private QuantityBuilder quantity;

    static ProgramMemoryBuilder none() {
        return new ProgramMemoryBuilder()
                .kind(NesMeta.Kind.NONE)
                .quantity(QuantityBuilder.banks8kb(0));

    }

    static ProgramMemoryBuilder battery8kb(int amount) {
        return new ProgramMemoryBuilder()
                .kind(NesMeta.Kind.PERSISTENT)
                .quantity(QuantityBuilder.banks8kb(amount));

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
