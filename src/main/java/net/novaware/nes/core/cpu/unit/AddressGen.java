package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.memory.SystemBus;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

public class AddressGen implements Unit {

    @Used
    private SystemBus systemBus;

    @Inject
    public AddressGen(SystemBus systemBus) {
        this.systemBus = systemBus;
    }

    public @Unsigned short fetchAddress(@Unsigned short address) {
        // TODO: implement a page crossing bug when indirect jumping
        @Unsigned byte addrLo = systemBus.specifyAnd(address).readByte();
        @Unsigned byte addrHi = systemBus.specifyAnd(ushort(uint(address) + 1)).readByte();
        return ushort(uint(addrHi) << 8 | uint(addrLo));
    }
}
