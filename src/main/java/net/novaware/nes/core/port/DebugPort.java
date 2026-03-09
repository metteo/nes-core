package net.novaware.nes.core.port;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface DebugPort extends InputPort, OutputPort {

    void connect(Receiver receiver);

    void disconnect();

    void setProgramCounter(@Unsigned short address);

    interface Receiver {
        void onException(Exception exception);
    }
}
