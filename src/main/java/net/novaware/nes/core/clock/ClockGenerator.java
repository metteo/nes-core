package net.novaware.nes.core.clock;

public interface ClockGenerator {

    void shutdown();

    @FunctionalInterface
    interface Handle {
        boolean cancel(boolean mayInterrupt);
    }

    Handle schedule(Runnable target, int frequency);
}
