package net.novaware.nes.core.port;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface JoypadPort extends InputPort {

    void connect(Plug plug);

    void disconnect();

    interface Plug {
        // TODO: DMC bug affecting joy input
        @Unsigned byte getState();
    }
}
