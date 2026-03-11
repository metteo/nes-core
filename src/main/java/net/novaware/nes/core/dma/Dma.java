package net.novaware.nes.core.dma;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.Synchronizable;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;

public class Dma { // TODO: remember about DMC DMA which is a different controller?

    enum State {
        /**
         * Waiting for commands
         */
        IDLE,
        /**
         * Halting the CPU
         */
        HALT,
        /**
         * Optional align with CPU (read-read / write - write)
         */
        ALIGN,
        /**
         * DMC specific, waiting for the bus to be ready
         */
        WAIT,
        /**
         * Read from the bus
         */
        READ,
        /**
         * Write to the bus
         */
        WRITE
    }

    @Owned
    private final ByteRegister oamDma;

    @Used
    private final Synchronizable cpu;

    @Used
    private final MemoryBus cpuBus;

    @Inject
    public Dma(
            @DmaVar(OAM) ByteRegister oamDma,
            Synchronizable cpu,
            @CpuVar(BUS) MemoryBus cpuBus
    ) {
        this.oamDma = oamDma;
        this.cpu = cpu;
        this.cpuBus = cpuBus;
    }

    public void triggerDma() {
        cpu.rdy(Signal.LOW);

        // halt cycle
        // align cycle* (if dma.read on cpu.write)
        // read cycle (256x)
        // write cycle (256x)

        cpu.rdy(Signal.HIGH);
    }
}
