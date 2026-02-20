package net.novaware.nes.core.easy;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.cpu.Cpu;
import net.novaware.nes.core.cpu.unit.InterruptLogic;
import net.novaware.nes.core.memory.MemoryBus;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.novaware.nes.core.cpu.memory.MemoryModule.CPU_BUS;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

public class EasyBoard {

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private @Nullable ScheduledFuture<?> future;

    private final Cpu cpu;
    private final MemoryBus bus;

    @Inject
    /* package */ EasyBoard(
        final Cpu cpu,
        final @Named(CPU_BUS) MemoryBus bus
    ) {
        this.cpu = cpu;
        this.bus = bus;
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

        future = executor.scheduleAtFixedRate(() -> {
            try {
                cpu.advance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 100, MILLISECONDS);
    }

    private void stop() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    public void preload(@Unsigned byte[] data) {
        int offset = sint(EasyMap.CARTRIDGE_START);
        for(int i = 0; i < data.length; i++) {
            bus.specifyThen(ushort(offset + i)).writeByte(data[i]);
        }

        int reset = sint(InterruptLogic.RES_VECTOR);
        bus.specifyThen(ushort(reset)).writeByte(ubyte(0x00));
        bus.specifyThen(ushort(reset + 1)).writeByte(ubyte(0x06)); // 0x600 in low endian

    }

    public void powerOff() {
        stop();
    }

    interface Drawing { void draw(int x, int y, int color); }
}
