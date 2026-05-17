package net.novaware.nes.core;

import jakarta.inject.Inject;
import net.novaware.nes.core.apu.Apu;
import net.novaware.nes.core.clock.ClockGenerator;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.port.CartridgePort;
import net.novaware.nes.core.port.DebugPort;
import net.novaware.nes.core.port.internal.DebugPortImpl;
import net.novaware.nes.core.ppu.Ppu;
import net.novaware.nes.core.util.uml.Owned;

@BoardScope
public class Board {

    public interface Config {
        boolean getRecordCpuBus();
        // TODO: review these, only keep them if the behaviour is different between values
        Region getRegion();
        Platform getPlatform();
        VideoStandard getVideoStandard();
    }

    // TODO: setup register files for cpu, ppu, apu and any other
    // TODO: any container of data should be wrapped in an object and injectable (e.g. into a debug interface)

    @Owned
    private final Cpu cpu;

    @Owned
    private final Ppu ppu;

    @Owned
    private final Apu apu;

    @Owned
    private final CartridgePort cartridgePort;

    @Owned
    private final DebugPortImpl debugPort;

    @Owned
    private final ClockGenerator clockGenerator;

    // TODO: include here RAM,

    @Inject
    /* package */ Board(
        final Cpu cpu,
        final Ppu ppu,
        final Apu apu,
        final CartridgePort cartridgePort,
        final DebugPortImpl debugPort,
        final ClockGenerator clockGenerator
    ) {
        this.cpu = cpu;
        this.ppu = ppu;
        this.apu = apu;

        this.cartridgePort = cartridgePort;
        this.debugPort = debugPort;
        this.clockGenerator = clockGenerator;
    }

    public void powerOn() {
        initialize();
        start();
    }

    private void start() {
        cpu.reset(Signal.LOW);
        ppu.reset(Signal.LOW);

        cpu.advance();
        ppu.cycle();

        cpu.reset(Signal.HIGH);
        ppu.reset(Signal.HIGH);

        clockGenerator.start();
    }

    public void powerOff() {
        stop();
    }

    private void stop() {
        clockGenerator.forceStop();
    }

    private void initialize() {
        cpu.initialize();
        ppu.initialize();
        apu.initialize();

        clockGenerator.setExceptionHandler(debugPort::onException);
    }

    public CartridgePort getCartridgePort() {
        return cartridgePort;
    }

    public DebugPort getDebugPort() {
        return debugPort;
    }
}
