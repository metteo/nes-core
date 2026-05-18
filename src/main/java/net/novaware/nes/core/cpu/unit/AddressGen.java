package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.cpu.register.CpuRegFile;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class AddressGen implements Unit {


    @Used
    private final CpuRegFile registers;

    @Used
    private final MemoryMgmt mmu;

    @Inject
    public AddressGen(
        CpuRegFile registers,
        MemoryMgmt mmu
    ) {
        this.registers = registers;
        this.mmu = mmu;
    }

    public @Unsigned short getPc() {
        var pc = registers.pc();

        int pcVal = pc.getAsInt();
        pc.setAsShort(pcVal + 1);

        return ushort(pcVal);
    }

    /**
     * Can cross pages
     */
    public @Unsigned short fetchAddress(@Unsigned short address) {
        @Unsigned byte addrLo = mmu.specifyAnd(address).readByte();
        @Unsigned byte addrHi = mmu.specifyAnd(ushort(sint(address) + 1)).readByte();
        return ushort(sint(addrHi) << 8 | sint(addrLo));
    }

    /**
     * Doesn't cross pages
     */
    public @Unsigned short fetchAddressPageWrap(@Unsigned short address) {
        int source = sint(address);

        int sourceLo = 0x00FF & source;
        int sourceHi = 0xFF00 & source;

        int sourcePlusOne = sourceHi | ((sourceLo + 1) & 0xFF);

        @Unsigned byte addrLo = mmu.specifyAnd(address).readByte();
        @Unsigned byte addrHi = mmu.specifyAnd(ushort(sourcePlusOne)).readByte();

        return ushort(sint(addrHi) << 8 | sint(addrLo));
    }
}
