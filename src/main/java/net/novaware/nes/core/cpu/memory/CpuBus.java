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
import net.novaware.nes.core.register.IntegerCounter;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.cpu.inject.CpuVarName.ACR;
import static net.novaware.nes.core.cpu.inject.CpuVarName.APU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.ATM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.CC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.DMA;
import static net.novaware.nes.core.cpu.inject.CpuVarName.IC;
import static net.novaware.nes.core.cpu.inject.CpuVarName.JOY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.PPU;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RAM;
import static net.novaware.nes.core.cpu.inject.CpuVarName.TMR;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class CpuBus implements MemoryBus {

    @Used
    private final IntegerCounter cycleCounter;
    private final IntegerCounter instructionCycle;

    @Owned
    private final PagedMemory internal = new PagedMemory("INTERNAL", CpuMemMap.MEMORY_SIZE, new MemoryDevice.Empty());

    @Used
    private MemoryDevice.ReadWrite cartridge = new MemoryDevice.Empty(); // TODO: consider using single injected instance to allow testing

    @Used
    private MemoryDevice.ReadWrite expansion = new MemoryDevice.Empty();

    private final DataLine dataLine = new DataLine();

    private BusOp busOp = BusOp.DATA_READ; // TODO: randomize between data read / write

    private @Unsigned short addressLatch;

    @Inject
    public CpuBus(
        @CpuVar(CC) IntegerCounter cycleCounter,
        @CpuVar(IC) IntegerCounter instructionCycle,
        @CpuVar(RAM) MemoryDevice.ReadWrite ram,
        @CpuVar(PPU) MemoryDevice.ReadWrite ppu,
        @CpuVar(ACR) MemoryDevice.WriteOnly apuChannelRegs,
        @CpuVar(DMA) MemoryDevice.WriteOnly oamDma,
        @CpuVar(APU) MemoryDevice.ReadWrite apuStatus,
        @CpuVar(JOY) MemoryDevice.ReadWrite joy, // TODO: joy
        @CpuVar(ATM) MemoryDevice.ReadWrite apuTest, // TODO: apu test
        @CpuVar(TMR) MemoryDevice.ReadWrite timer // TODO: timer
    ) {
        this.cycleCounter = cycleCounter;
        this.instructionCycle = instructionCycle;

        MemoryPage page40 = new MemoryPage(ubyte(0x40), new MemoryDevice.Empty());
        page40.attach(apuChannelRegs);
        page40.attach(oamDma);
        page40.attach(apuStatus);
        page40.attach(joy);
        page40.attach(apuTest);
        page40.attach(timer);

        internal.attach(ram);
        internal.attach(ppu);
        internal.attach(page40);

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
        instructionCycle.increment();

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

    @Override // TODO: verify that the bus is reverted to correct state
    public @Unsigned byte peek(@Unsigned short address) {
        // perform the read
        internal.onAccess(address);
        cartridge.onAccess(address);
        expansion.onAccess(address);

        internal.onRead();
        cartridge.onRead();
        expansion.onRead();

        var peeked = dataLine.cycle();

        // return the bus to the previous state
        internal.onAccess(addressLatch);
        cartridge.onAccess(addressLatch);
        expansion.onAccess(addressLatch);

        // TODO: what if previous op was write?
        internal.onRead();
        cartridge.onRead();
        expansion.onRead();

        dataLine.cycle();

        // return the data
        return peeked;
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
