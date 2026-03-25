package net.novaware.nes.core.ppu.memory;

import jakarta.inject.Inject;
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

@SuppressWarnings({"initialization.fields.uninitialized", "return"}) // TODO: remove when fully implemented
public class PpuBus implements MemoryBus {

    @Used
    private CycleCounter cycleCounter;

    @Owned
    private final PagedMemory internal = new PagedMemory("INTERNAL", new MemoryDevice.Empty());

    @Used
    private MemoryDevice.ReadWrite cartridge = new MemoryDevice.Empty(); // TODO: consider using single injected instance to allow testing

    @Used
    private MemoryDevice.ReadWrite expansion = new MemoryDevice.Empty();

    private final DataLine dataLine = new DataLine();

    private BusOp busOp = BusOp.DATA_READ; // TODO: randomize between data read / write

    private @Unsigned short addressLatch;

    @Inject
    public PpuBus() {
        // TODO: Pattern tables from cartridge
        // TODO: nametables (vram) with mirroring
        // TODO: pallete
    }

    @Override
    public BusOp currentOp() {
        return busOp;
    }

    @Override
    public void attachCartridge(MemoryDevice.ReadWrite cartridge) {

    }

    @Override
    public void detachCartridge() {

    }

    @Override
    public void attachExpansion(MemoryDevice.ReadWrite expansion) {

    }

    @Override
    public void detachExpansion() {

    }

    @Override
    public ControlBus.Line access(@Unsigned short address) {
        return null;
    }

    @Override
    public DataBus.Read read() {
        return null;
    }

    @Override
    public DataBus.Write write() {
        return null;
    }

    @Override
    public @Unsigned byte data() {
        return 0;
    }

    @Override
    public void data(@Unsigned byte data) {

    }
}
