package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;

/**
 * Pre-fetches instruction opcode
 */
@BoardScope
public class PrefetchUnit implements Unit, Runnable {

    @Used private final ByteRegister currentInstruction;

    @Used private final AddressGen agu;
    @Used private final MemoryMgmt mmu;

    @Inject
    public PrefetchUnit(
        @CpuVar(CI) ByteRegister currentInstruction,
        AddressGen agu,
        MemoryMgmt mmu
    ) {
        this.currentInstruction = currentInstruction;
        this.agu = agu;
        this.mmu = mmu;
    }

    public void run() {
        @Unsigned short opcodeAddress = agu.getPc();
        @Unsigned byte opcode = mmu.specifyAnd(opcodeAddress).readByte();

        //System.out.print(Hex.s(opcodeAddress).toUpperCase() + "  " + Hex.s(opcode).toUpperCase() + " ");

        currentInstruction.set(opcode);
    }
}
