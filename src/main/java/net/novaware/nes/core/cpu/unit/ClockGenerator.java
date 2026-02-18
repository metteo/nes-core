package net.novaware.nes.core.cpu.unit;

public interface ClockGenerator extends Unit {

    void shutdown();

    @FunctionalInterface
    interface Handle {
        boolean cancel(boolean mayInterrupt);
    }

    Handle schedule(Runnable target, int frequency);
}
