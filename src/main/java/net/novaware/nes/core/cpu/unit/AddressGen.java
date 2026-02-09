package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.CpuModule.CPU_BUS;
import static net.novaware.nes.core.util.UnsignedTypes.uint;
import static net.novaware.nes.core.util.UnsignedTypes.ushort;

public class AddressGen implements Unit {


    @Used
    private final CpuRegisters registers;

    @Used
    private final MemoryBus memoryBus; // TODO: use MMU

    @Inject
    public AddressGen(
        CpuRegisters registers,
        @Named(CPU_BUS) MemoryBus systemBus
    ) {
        this.registers = registers;
        this.memoryBus = systemBus;
    }

    public @Unsigned short getPc() {
        var pc = registers.pc();

        int pcVal = pc.getAsInt();
        pc.setAsShort(pcVal + 1);

        return ushort(pcVal);
    }

    public @Unsigned short fetchAddress(@Unsigned short address) {
        @Unsigned byte addrLo = memoryBus.specifyAnd(address).readByte();
        @Unsigned byte addrHi = memoryBus.specifyAnd(ushort(uint(address) + 1)).readByte();
        return ushort(uint(addrHi) << 8 | uint(addrLo));
    }

    public @Unsigned short buggyFetchAddress(@Unsigned short address) {
        int source = uint(address);

        int sourceLo = 0x00FF & source;
        int sourceHi = 0xFF00 & source;

        int sourcePlusOne = sourceHi | ((sourceLo + 1) & 0xFF);

        @Unsigned byte addrLo = memoryBus.specifyAnd(address).readByte();
        @Unsigned byte addrHi = memoryBus.specifyAnd(ushort(sourcePlusOne)).readByte();
        return ushort(uint(addrHi) << 8 | uint(addrLo));
    }
}
