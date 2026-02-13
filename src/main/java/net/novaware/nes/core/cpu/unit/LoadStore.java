package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.register.DataRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;

public class LoadStore implements Unit {

    @Used private final CpuRegisters registers;

    @Inject
    public LoadStore(CpuRegisters registers) {
        this.registers = registers;
    }

    void load(DataRegister register) {
        @Unsigned byte data = registers.dor().getData();

        register.set(data);

        int dataVal = uint(data);

        registers.status()
                .setZero(dataVal == 0)
                .setNegative((dataVal & 0x80) != 0);
    }

    void store(DataRegister register) {
        @Unsigned byte data = register.get();

        registers.dor().setData(data);
    }
}
