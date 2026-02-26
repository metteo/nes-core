package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.CpuRegisters;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Pre-fetches instruction opcode
 */
@BoardScope
public class PrefetchUnit implements Unit, Runnable {

    @Used private final CpuRegisters registers;
    @Used private final AddressGen agu;
    @Used private final MemoryMgmt mmu;

    @Inject
    public PrefetchUnit(
        CpuRegisters registers,
        AddressGen agu,
        MemoryMgmt mmu
    ) {
        this.registers = registers;
        this.agu = agu;
        this.mmu = mmu;
    }

    public void run() {
        @Unsigned short opcodeAddress = agu.getPc();
        @Unsigned byte opcode = mmu.specifyAnd(opcodeAddress).readByte();

        System.out.print(Hex.s(opcodeAddress).toUpperCase() + "  " + Hex.s(opcode).toUpperCase() + " ");

        registers.cir().set(opcode);
    }
}
