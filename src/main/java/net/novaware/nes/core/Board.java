package net.novaware.nes.core;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.unit.ClockGenerator;
import net.novaware.nes.core.cpu.unit.ClockGenerator.Handle;
import net.novaware.nes.core.port.CartridgePort;
import net.novaware.nes.core.util.uml.Owned;
import net.novaware.nes.core.util.uml.Used;

import java.util.ArrayList;
import java.util.List;

@BoardScope
public class Board {
    // TODO: setup register files for cpu, ppu, apu and any other
    // TODO: any container of data should be wrapped in an object and injectable (e.g. into a debug interface)

    @Owned
    private final Cpu cpu;

    @Used
    private final ClockGenerator clock;

    @Owned
    private final List<Handle> clockHandles = new ArrayList<>();

    /*
    @Owned
    @Named(CPU_RAM)
    private final PhysicalMemory cpuMemory;
    */
    @Inject
    /* package */ Board(
        final Cpu cpu,
        final ClockGenerator clock
    ) {
        this.cpu = cpu;
        this.clock = clock;
    }

    public void powerOn() {
        initialize();
        start();
    }

    private void start() {
        Handle cpuHandle = clock.schedule(cpu::advance, 5);

        clockHandles.add(cpuHandle);
    }

    private void stop() {
        clockHandles.forEach(handle -> handle.cancel(true));
        clock.shutdown();
    }

    private void initialize() {
        cpu.initialize();
    }

    public CartridgePort getCartridgePort() {
        throw new UnsupportedOperationException("not implemented!");
    }
}
