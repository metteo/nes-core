package net.novaware.nes.core.ppu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.BoardScope;
import net.novaware.nes.core.cpu.memory.CpuBusBridge;
import net.novaware.nes.core.cpu.memory.CpuMemMap;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.OpenLine;
import net.novaware.nes.core.ppu.inject.PpuVar;
import net.novaware.nes.core.ppu.register.PpuStatusRegister;
import net.novaware.nes.core.ppu.register.ViewPortRegister;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_END;
import static net.novaware.nes.core.cpu.memory.CpuMemMap.PPU_REGISTERS_MIRROR_END;
import static net.novaware.nes.core.ppu.inject.PpuVarName.BUS;
import static net.novaware.nes.core.ppu.inject.PpuVarName.T;
import static net.novaware.nes.core.ppu.inject.PpuVarName.VX;
import static net.novaware.nes.core.ppu.inject.PpuVarName.W;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ushort;

@BoardScope
public class PpuMemDevice implements MemoryDevice.ReadWrite, Nameable, CpuBusBridge {

    private final MemoryBus ppuBus; // TODO: probably shouldn't have direct access, instead through ppu?
    private final ViewPortRegister currentViewPort;
    private final ViewPortRegister tempViewPort;
    private final BooleanRegister writeRegister;

    // TODO: direct < 0x3EFF traffic to PpuBus, >0x3F00 to Palette RAM

    private final Handler emptyHandler = new EmptyHandler();
    private Handler[] readHandlers = new Handler[8];
    private Handler[] writeHandlers = new Handler[8];

    private Handler readHandlerLatch;
    private Handler writeHandlerLatch;

    private DataBus.Line dataLine = new OpenLine();

    private @Unsigned short addressLatch;

    @Inject
    public PpuMemDevice(
        @PpuVar(BUS) MemoryBus ppuBus,
        @PpuVar(VX) ViewPortRegister currentViewPort,
        @PpuVar(T) ViewPortRegister tempViewPort,
        @PpuVar(W) BooleanRegister writeRegister,
        PpuStatusRegister statusRegister
    ) {
        this.ppuBus = ppuBus;
        this.currentViewPort = currentViewPort;
        this.tempViewPort = tempViewPort;
        this.writeRegister = writeRegister;

        for (int i = 0; i < readHandlers.length; i++) {
            readHandlers[i] = emptyHandler;
            writeHandlers[i] = emptyHandler;
        }

        readHandlerLatch = emptyHandler;
        writeHandlerLatch = emptyHandler;

        writeHandlers[0] = new ControlHandler();
        writeHandlers[1] = new MaskHandler();
        readHandlers [2] = new StatusHandler();
        writeHandlers[3] = new OamAddressHandler();
        writeHandlers[4] = new OamDataHandler();
        readHandlers [4] = new OamDataHandler();
        writeHandlers[5] = new ScrollHandler();
        writeHandlers[6] = new AddressBusHandler();
        writeHandlers[7] = new DataBusHandler();
        readHandlers [7] = new DataBusHandler();
    }

    @Override
    public String getName() {
        return "CPU<->PPU";
    }

    @Override
    public @Unsigned short getStartAddress() {
        return CpuMemMap.PPU_REGISTERS_START;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return PPU_REGISTERS_MIRROR_END;
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onAccess(@Unsigned short address) {
        final int addrInt = sint(address);

        addressLatch = ushort(addrInt & sint(PPU_REGISTERS_END));

        readHandlerLatch = readHandlers[addrInt & 0x7];
        writeHandlerLatch = writeHandlers[addrInt & 0x7];
    }

    @Override
    public void onRead() {
        readHandlerLatch.onRead();
    }

    @Override
    public void onWrite() {
        writeHandlerLatch.onWrite();
    }

    @Override
    public void onDetach() {
        dataLine = new OpenLine();
    }

    public interface Handler {

        default void onRead() {}

        default void onWrite() {}
    }

    class EmptyHandler implements Handler {}

    class ControlHandler implements Handler {}

    class MaskHandler implements Handler {}

    class StatusHandler implements Handler {

        @Override
        public void onRead() {
            // 5 bits openbus or 2C05 PPU ID

            @Unsigned byte status = UBYTE_0; // TODO: read status
            dataLine.data(status);

            writeRegister.set(false);
            // TODO: clear vblank

        }
    }

    class OamAddressHandler implements Handler {}

    class OamDataHandler implements Handler {}

    class ScrollHandler implements Handler {

        @Override
        public void onWrite() {
            @Unsigned byte data = dataLine.data();

            final boolean firstWrite = !writeRegister.get();
            if (firstWrite) {
                // TODO: update t.coarseX and v.fineX
                writeRegister.set(true);
            } else {
                // TODO: update t.coarseY and t.fineY
                writeRegister.set(false);

                currentViewPort.set(tempViewPort.get()); // FIXME: do we transfer t -> v?
            }
        }
    }

    class AddressBusHandler implements Handler {

        @Override
        public void onWrite() {
            @Unsigned byte data = dataLine.data();

            final boolean firstWrite = !writeRegister.get();
            if (firstWrite) {
                tempViewPort.high(data);
                writeRegister.set(true);
            } else {
                tempViewPort.low(data);
                writeRegister.set(false);

                currentViewPort.set(tempViewPort.get());
            }
        }
    }

    class DataBusHandler implements Handler {

        @Override
        public void onRead() {
            // TODO: data is not available immediately. it takes one ppu cycle to fetch it for cpu
            @Unsigned byte data = ppuBus.access(currentViewPort.get()).read().data();
            dataLine.data(data);
        }

        @Override
        public void onWrite() {
            @Unsigned byte data = dataLine.data();
            // FIXME: verify if the data is written immediately or if ppu needs a cycle to write it?
            // may depend on memory area (internal ppu or cart)
            ppuBus.access(currentViewPort.get()).write().data(data);
        }
    }
}
