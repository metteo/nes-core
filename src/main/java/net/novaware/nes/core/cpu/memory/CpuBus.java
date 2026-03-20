package net.novaware.nes.core.cpu.memory;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.memory.BusOp;
import net.novaware.nes.core.memory.ControlBus;
import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.DataLine;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.MemoryPage;
import net.novaware.nes.core.memory.PagedMemory;
import net.novaware.nes.core.register.CycleCounter;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.APU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ATM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.TMR;

public class CpuBus implements MemoryBus {

    @Used
    private final CycleCounter cycleCounter;

    private final DataLine dataLine = new DataLine();

    private BusOp busOp = BusOp.DATA_READ; // TODO: randomize between data read / write

    @Owned
    private final PagedMemory internal;

    @Used
    private MemoryDevice.ReadWrite cartridge = new MemoryDevice.Empty();

    @Used
    private MemoryDevice.ReadWrite expansion = new MemoryDevice.Empty();

    @Owned
    private final MemoryPage page40;

    @Used
    private MemoryDevice ram;

    @Used
    private MemoryDevice ppu;

    @Used
    private MemoryDevice apu;

    @Used
    private MemoryDevice apuTest;

    @Used
    private MemoryDevice timer;

    private @Unsigned short addressLatch;

    @Inject
    public CpuBus(
            @CpuVar(CC) CycleCounter cycleCounter,
            @CpuVar(RAM) MemoryDevice.ReadWrite ram,
            @CpuVar(PPU) MemoryDevice.ReadWrite ppu,
            @CpuVar(APU) MemoryDevice.ReadWrite apu,
            @CpuVar(ATM) MemoryDevice.ReadWrite apuTest, // TODO: apu test
            @CpuVar(TMR) MemoryDevice.ReadWrite timer // TODO: timer
    ) {
        this.internal = new PagedMemory(new MemoryDevice.Empty());

        this.page40 = new MemoryPage(40, new MemoryDevice.Empty()); // TODO: handle the fallback, preferably open bus

        this.cycleCounter = cycleCounter;
        this.ram = ram;
        this.ppu = ppu;
        this.apu = apu;
        this.apuTest = apuTest;
        this.timer = timer;

        internal.attach(ram);
        internal.attach(ppu);
        internal.attach(page40);

        page40.attach(apu);
        page40.attach(apuTest);
        page40.attach(timer);

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
    public void attachExpansion(MemoryDevice.ReadWrite expansion) {
        this.expansion = expansion;
        this.expansion.onAttach(dataLine);
    }

    @Override
    public void detachExpansion() {
        expansion.onDetach();
        expansion = new MemoryDevice.Empty();
    }

    @Override
    public ControlBus.Line access(@Unsigned short address) {
        assert busOp == BusOp.DATA_READ || busOp == BusOp.DATA_WRITE; // compile out, TODO: consider JCP or Manifold

        busOp = BusOp.ADDRESS_ACCESS;
        addressLatch = address;
        cycleCounter.increment();

        internal.onAccess(addressLatch);
        cartridge.onAccess(addressLatch);
        expansion.onAccess(addressLatch);

        return this;
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
        expansion.onRead();

        return dataLine.cycle(); // CPU reading the line
    }

    @Override
    public void data(@Unsigned byte data) {
        assert busOp == BusOp.CONTROL_WRITE; // compile out

        busOp = BusOp.DATA_WRITE;

        dataLine.data(data); // CPU driving the line

        internal.onWrite();
        cartridge.onWrite();
        expansion.onWrite();

        dataLine.cycle();
    }
}
