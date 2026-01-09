package net.novaware.nes.core.file;

import net.novaware.nes.core.util.QuantityBuilder;

class ProgramMemoryBuilder implements TestDataBuilder<NesFile.ProgramMemory> {

    private NesFile.Kind kind;
    private QuantityBuilder quantity;

    static ProgramMemoryBuilder none() {
        return new ProgramMemoryBuilder()
                .kind(NesFile.Kind.NONE)
                .quantity(QuantityBuilder.banks8kb(0));

    }

    ProgramMemoryBuilder kind(NesFile.Kind kind) {
        this.kind = kind;
        return this;
    }

    ProgramMemoryBuilder quantity(QuantityBuilder quantity) {
        this.quantity = quantity;
        return this;
    }

    @Override
    NesFile.ProgramMemory build() {
        return new NesFile.ProgramMemory(kind, quantity.build());
    }
}
