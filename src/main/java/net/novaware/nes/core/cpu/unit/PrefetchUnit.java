package net.novaware.nes.core.cpu.unit;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.register.ShortRegister;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CI;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PA;

/**
 * Pre-fetches instruction opcode
 */
@BoardScope
public class PrefetchUnit implements Unit, Runnable {

    private final CycleCounter cycleCounter;
    @Used private final ShortRegister prefetchAddress;
    @Used private final ByteRegister currentInstruction;

    @Used private final AddressGen agu;
    @Used private final MemoryMgmt mmu;

    @Inject
    public PrefetchUnit(
        @CpuVar(CC) CycleCounter cycleCounter,
        @CpuVar(PA) ShortRegister prefetchAddress,
        @CpuVar(CI) ByteRegister currentInstruction,
        AddressGen agu,
        MemoryMgmt mmu
    ) {
        this.cycleCounter = cycleCounter;
        this.prefetchAddress = prefetchAddress;
        this.currentInstruction = currentInstruction;
        this.agu = agu;
        this.mmu = mmu;
    }

    public void run() {
        cycleCounter.subReset();

        // TODO: consider pulling SYNC line to HIGH here, instead of cpu?
        // sync is not triggered during reset sequence?
        @Unsigned short opcodeAddress = agu.getPc();
        @Unsigned byte opcode = mmu.specifyAnd(opcodeAddress).readByte();

        prefetchAddress.set(opcodeAddress);
        currentInstruction.set(opcode);
    }
}
