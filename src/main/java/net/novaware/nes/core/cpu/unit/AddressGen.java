package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_BUS;
import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

public class AddressGen implements Unit {

    @Used
    private final MemoryBus memoryBus;

    @Inject
    public AddressGen(@Named(CPU_BUS) MemoryBus systemBus) {
        this.memoryBus = systemBus;
    }

    public @Unsigned short fetchAddress(@Unsigned short address) {
        // TODO: implement a page crossing bug when indirect jumping
        @Unsigned byte addrLo = memoryBus.specifyAnd(address).readByte();
        @Unsigned byte addrHi = memoryBus.specifyAnd(ushort(uint(address) + 1)).readByte();
        return ushort(uint(addrHi) << 8 | uint(addrLo));
    }
}
