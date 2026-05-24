package net.novaware.nes.core.port;

import net.novaware.nes.core.ppu.memory.DisplayMemory;

public interface DisplayPort extends OutputPort {

    void connect(Plug plug);

    void disconnect();

    interface Plug {
        void onDisplayData(DisplayMemory frontBuffer);
    }
}
