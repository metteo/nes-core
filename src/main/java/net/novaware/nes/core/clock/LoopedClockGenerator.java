package net.novaware.nes.core.clock;

import net.novaware.nes.core.cpu.unit.ClockGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoopedClockGenerator implements ClockGenerator {

    private final ExecutorService executor;

    public LoopedClockGenerator() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    // NOTE: gemini generated method for early testing
    @Override
    public Handle schedule(Runnable target, int frequency) {
        final AtomicBoolean running = new AtomicBoolean(true);

        long delayNanos = 1_000_000_000L / frequency;

        final Future<?> future = executor.submit(() -> {
            try {
                while (running.get() && !Thread.currentThread().isInterrupted()) {
                    long start = System.nanoTime();

                    target.run();

                    long end = System.nanoTime();
                    long elapsed = end - start;
                    long sleepTime = delayNanos - elapsed;

                    if (sleepTime > 0) {
                        long millis = sleepTime / 1_000_000;
                        int nanos = (int) (sleepTime % 1_000_000);
                        Thread.sleep(millis, nanos);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return force -> {
            running.set(false);
            return future.cancel(force);
        };
    }
}
