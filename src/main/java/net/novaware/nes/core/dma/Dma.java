package net.novaware.nes.core.dma;

import dagger.Lazy;
import jakarta.inject.Inject;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.memory.CpuMemMap;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.ushort;

public class Dma implements ClockReceiver { // TODO: remember about DMC DMA which is a different controller?

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

    // TODO: replace direct reference with a signal sender?
    @Used
    private final Lazy<Cpu> cpu; // top level chip so lazy injected to prevent stack overflow during construction

    @Used
    private final MemoryBus cpuBus;

    @Inject
    public Dma(
            @DmaVar(OAM) ByteRegister oamDma,
            Lazy<Cpu> cpu,
            @CpuVar(BUS) MemoryBus cpuBus
    ) {
        this.oamDma = oamDma;
        this.cpu = cpu;
        this.cpuBus = cpuBus;
    }

    public void triggerDma() {
        cpu.get().rdy(Signal.LOW);

        // halt cycle
        // align cycle* (if dma.read on cpubus.write)

        // read cycle   \
        //               > (256x)
        // write cycle  /

        int startAddress = oamDma.getAsInt() << 8;
        cpuBus.access(CpuMemMap.PPU_OAM_ADDRESS_REGISTER).write().data(UBYTE_0);

        for(int i = 0; i < 256; i++) {
            int address = startAddress | i;
            @Unsigned byte data = cpuBus.access(ushort(address)).read().data();
            cpuBus.access(CpuMemMap.PPU_OAM_DATA_REGISTER).write().data(data);
        }

        cpu.get().rdy(Signal.HIGH);
    }

    @Override
    public int cycle() {
        return 0;
    }
}
