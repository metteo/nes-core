package net.novaware.nes.core.clock;

import java.util.function.Consumer;

public interface ClockGenerator {
    void start();
    void stop();
    void forceStop();

    void setExceptionHandler(Consumer<Exception> exceptionHandler);
}
