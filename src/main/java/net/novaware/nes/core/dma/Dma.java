package net.novaware.nes.core.dma;

import dagger.Lazy;
import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.memory.CpuBus;
import net.novaware.nes.core.cpu.memory.CpuMemMap;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.dma.inject.DmaVar;
import net.novaware.nes.core.memory.BusOp;
import net.novaware.nes.core.register.ByteRegister;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_OAM_ADDRESS_REGISTER;
import static net.novaware.nes.core.dma.Dma.State.ALIGN;
import static net.novaware.nes.core.dma.Dma.State.HALT;
import static net.novaware.nes.core.dma.Dma.State.IDLE;
import static net.novaware.nes.core.dma.Dma.State.READ;
import static net.novaware.nes.core.dma.Dma.State.WRITE;
import static net.novaware.nes.core.dma.inject.DmaVarName.OAM;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
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

    private State state = IDLE;

    @Owned
    private final ByteRegister oamDma;

    // TODO: consider exposing those in DmaRegModule
    @Owned
    private final ByteRegister offset = new ByteRegister("DMA.OFFSET");

    @Owned final ByteRegister buffer = new ByteRegister("DMA.BUFFER");

    // TODO: replace direct reference with a signal sender?
    @Used
    private final Lazy<Cpu> cpu; // top level chip so lazy injected to prevent stack overflow during construction

    @Used
    private final CpuBus cpuBus;

    @Inject
    public Dma(
            @DmaVar(OAM) ByteRegister oamDma,
            Lazy<Cpu> cpu,
            CpuBus cpuBus
    ) {
        this.oamDma = oamDma;
        this.cpu = cpu;
        this.cpuBus = cpuBus;
    }

    State getState() {
        return state;
    }

    public void triggerDma() {
        assert state == IDLE : "should be IDLE when triggering";

        state = HALT;
    }

    @Override
    public int cycle() {
        switch(state) {
            case IDLE -> { return 0; } // cycles
            case HALT -> {
                cpu.get().rdy(Signal.LOW);

                switch(cpuBus.currentOp()) {
                    case BusOp.DATA_READ -> {
                        cpuBus.access(PPU_OAM_ADDRESS_REGISTER).write().data(UBYTE_0);
                        state = READ;
                    }
                    case BusOp.DATA_WRITE -> {
                        cpuBus.access(PPU_OAM_ADDRESS_REGISTER).read().data();
                        state = ALIGN;
                    }

                    default -> throw new IllegalStateException("cpuBus has invalid currentOp");
                }
            }
            case ALIGN -> {
                cpuBus.access(PPU_OAM_ADDRESS_REGISTER).write().data(UBYTE_0);
                state = READ;
            }

            case READ -> {
                int offset = this.offset.getAsInt();
                int address = (oamDma.getAsInt() << 8) | offset;
                @Unsigned byte data = cpuBus.access(ushort(address)).read().data();
                buffer.set(data);

                this.offset.setAsByte(offset + 1);
                state = WRITE;
            }

            case WRITE -> {
                cpuBus.access(CpuMemMap.PPU_OAM_DATA_REGISTER).write().data(buffer.get());

                if (offset.get() == UBYTE_0) { // last write
                    state = IDLE;
                    cpu.get().rdy(Signal.HIGH);
                } else {
                    state = READ;
                }
            }
        }

        return 1;
    }
}
