package net.novaware.nes.core.easy.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.BusOp;
import net.novaware.nes.core.memory.ControlBus;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.DataLine;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.PagedMemory;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.SS;

/**
 * Easy 6502 Bus implementation
 *
 * @see <a href="https://skilldrick.github.io/easy6502/">Easy6502 by skilldrick</a>
 */
public class EasyBus implements MemoryBus {

    @Owned
    private final PagedMemory internal = new PagedMemory("INTERNAL", new MemoryDevice.Empty());
    private final CycleCounter cycleCounter;

    @Used
    private MemoryDevice.ReadWrite cartridge = new MemoryDevice.Empty();

    private final DataLine dataLine = new DataLine();

    private BusOp busOp = BusOp.DATA_READ;

    private @Unsigned short addressLatch;

    @Inject
    public EasyBus(
        @CpuVar(CC)  CycleCounter cycleCounter,
        @CpuVar(RAM) MemoryDevice.ReadWrite ram,
        @CpuVar(SS)  MemoryDevice.ReadWrite stack,
        @CpuVar(PPU) MemoryDevice.ReadWrite vram
    ) {
        this.cycleCounter = cycleCounter;

        internal.attach(ram);
        internal.attach(stack);
        internal.attach(vram);

        internal.onAttach(dataLine);
    }

    @Override
    public BusOp currentOp() {
        return busOp;
    }

    @Override
    public void attachCartridge(MemoryDevice.ReadWrite cartridge) {
        this.cartridge = cartridge;
        this.cartridge.onAttach(dataLine);
    }

    @Override
    public void detachCartridge() {
        cartridge.onDetach();
        cartridge = new MemoryDevice.Empty();
    }

    @Override
    public void attachExpansion(MemoryDevice.ReadWrite expansion) {}

    @Override
    public void detachExpansion() {}

    @Override
    public ControlBus.Line access(@Unsigned short address) {
        assert busOp == BusOp.DATA_READ || busOp == BusOp.DATA_WRITE;

        busOp = BusOp.ADDRESS_ACCESS;
        addressLatch = address;
        cycleCounter.increment();

        internal.onAccess(addressLatch);
        cartridge.onAccess(addressLatch);

        return this;
    }

    @Override
    public @Unsigned byte peek(@Unsigned short address) {
        internal.onAccess(address);
        cartridge.onAccess(address);

        internal.onRead();
        cartridge.onRead();

        return dataLine.cycle();
    }

    @Override
    public DataBus.Read read() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out

        busOp = BusOp.CONTROL_READ;

        return this;
    }

    @Override
    public DataBus.Write write() {
        assert busOp == BusOp.ADDRESS_ACCESS; // compile out

        busOp = BusOp.CONTROL_WRITE;

        return this;
    }

    @Override
    public @Unsigned byte data() {
        assert busOp == BusOp.CONTROL_READ; // compile out

        busOp = BusOp.DATA_READ;

        internal.onRead();
        cartridge.onRead();

        return dataLine.cycle(); // CPU reading the line
    }

    @Override
    public void data(@Unsigned byte data) {
        assert busOp == BusOp.CONTROL_WRITE; // compile out

        busOp = BusOp.DATA_WRITE;

        dataLine.data(data); // CPU driving the line

        internal.onWrite();
        cartridge.onWrite();

        dataLine.cycle();
    }
}
