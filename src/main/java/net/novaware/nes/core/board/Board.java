package net.novaware.nes.core.board;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.apu.Apu;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockGenerator;
import net.novaware.nes.core.config.Platform;
import net.novaware.nes.core.config.Region;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.cpu.signal.internal.LevelDetector;
import net.novaware.nes.core.pin.Pin;
import net.novaware.nes.core.port.CartridgePort;
import net.novaware.nes.core.port.DebugPort;
import net.novaware.nes.core.port.internal.DebugPortImpl;
import net.novaware.nes.core.ppu.Ppu;
import net.novaware.nes.core.util.uml.Owned;

// TODO: thread safety! Make this an interface which has 2 implementations:
//       - business logic, which assumes execution within single thread
//       - thread separation logic, where call from separate thread get submitted to executor
//       Port are a boundary between external world and internal thread safe env
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

    @Owned
    private final Pin reset;

    // TODO: include here RAM,

    @Inject
    /* package */ Board(
        final Cpu cpu,
        final Ppu ppu,
        final Apu apu,
        final CartridgePort cartridgePort,
        final DebugPortImpl debugPort,
        final ClockGenerator clockGenerator,
        final @Named("BRD.RST") LevelDetector reset
    ) {
        this.cpu = cpu;
        this.ppu = ppu;
        this.apu = apu;

        this.cartridgePort = cartridgePort;
        this.debugPort = debugPort;
        this.clockGenerator = clockGenerator;
        this.reset = reset;
    }

    public void powerOn() {
        initialize();
        start();
    }

    private void start() {
        reset.set(Signal.LOW);

        cpu.advance();
        ppu.cycle();

        reset.set(Signal.HIGH);

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
