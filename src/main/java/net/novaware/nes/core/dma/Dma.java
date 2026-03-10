package net.novaware.nes.core.dma;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.Synchronizable;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;
import static net.novaware.nes.core.util.Asserts.assertArgument;
import static net.novaware.nes.core.util.UTypes.sint;

public class Dma implements MemoryDevice { // TODO: remember about DMC DMA which is a different controller?

    enum State {
        HALT, ALIGN, READ, WRITE
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

    @Override
    public void specify(@Unsigned short address) {
        assertArgument(sint(address) == 0x4014, "OAM DMA responds only to 0x4014"); // TODO: Refer to memory map or accept as param?
    }

    @Override
    public @Unsigned byte readByte() {
        return 0; // TODO: open bus
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        oamDma.set(data);

        triggerDma();
    }

    private void triggerDma() {
        cpu.rdy(Signal.LOW);

        // halt cycle
        // align cycle* (if dma.read on cpu.write)
        // read cycle (256x)
        // write cycle (256x)

        cpu.rdy(Signal.HIGH);
    }
}
