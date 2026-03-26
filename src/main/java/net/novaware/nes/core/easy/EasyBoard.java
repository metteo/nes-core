package net.novaware.nes.core.easy;

import jakarta.inject.Inject;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.inject.CpuVar;
import net.novaware.nes.core.cpu.signal.Signal;
import net.novaware.nes.core.memory.MemoryBus;
import net.novaware.nes.core.memory.PhysicalMemory;
import net.novaware.nes.core.register.DelegatingRegister;
import net.novaware.nes.core.util.UByteBuffer;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.BUS;
import static net.novaware.nes.core.cpu.inject.CpuVarName.JOY;
import static net.novaware.nes.core.cpu.inject.CpuVarName.RNG;
import static net.novaware.nes.core.easy.memory.EasyMemMap.CARTRIDGE_END;
import static net.novaware.nes.core.easy.memory.EasyMemMap.CARTRIDGE_SIZE;
import static net.novaware.nes.core.easy.memory.EasyMemMap.CARTRIDGE_START;
import static net.novaware.nes.core.easy.memory.EasyMemMap.RES_VECTOR;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

public class EasyBoard {

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private @Nullable ScheduledFuture<?> future;

    private final Cpu cpu;
    private final MemoryBus bus;

    private final DelegatingRegister rng;
    private final DelegatingRegister joy;

    @Inject
    /* package */ EasyBoard(
        final Cpu cpu,
        final @CpuVar(BUS) MemoryBus bus,
        final @CpuVar(RNG) DelegatingRegister rng,
        final @CpuVar(JOY) DelegatingRegister joy
    ) {
        this.cpu = cpu;
        this.bus = bus;
        this.rng = rng;
        this.joy = joy;
    }

    public void powerOn() {
        initialize();
        start();
    }

    private void initialize() {
        cpu.initialize();
    }

    private void start() {
        if (future != null) {
            return;
        }

        cpu.reset(Signal.LOW);
        cpu.advance();
        cpu.reset(Signal.HIGH);

        future = executor.scheduleAtFixedRate(() -> {
            try {
                // TODO: update rng
                // TODO: update key
                cpu.advance();
                // TODO: update display
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 15, MILLISECONDS);
    }

    private void stop() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    public void preload(@Unsigned byte[] data) {
        UByteBuffer buffer = UByteBuffer.allocate(CARTRIDGE_SIZE);
        buffer.put(0, data);

        PhysicalMemory cartridge = new PhysicalMemory("CART", CARTRIDGE_START, CARTRIDGE_END, buffer);
        bus.attachCartridge(cartridge);

        int reset = sint(RES_VECTOR);
        bus.access(ushort(reset)).write().data(ubyte(0x00));
        bus.access(ushort(reset + 1)).write().data(ubyte(0x06)); // 0x600 in low endian
    }

    public void powerOff() {
        stop();
    }

    interface Drawing { void draw(int x, int y, int color); }
}
