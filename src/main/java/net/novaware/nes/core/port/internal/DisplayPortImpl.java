package net.novaware.nes.core.port.internal;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.port.DisplayPort;
import net.novaware.nes.core.ppu.memory.DisplayMemory;

// TODO: display port should expose BufferedImage or multiple with separate layers
@BoardScope
@SuppressWarnings("initialization.fields.uninitialized")
public class DisplayPortImpl implements DisplayPort {

    private Plug plug = displayMemory -> {};

    // TODO: this is too early for access to DisplayMemory, it still needs processing -> turning into RGB/NTSC
    private DisplayMemory displayMemory;

    @Inject
    public DisplayPortImpl(
        DisplayMemory displayMemory
    ) {
        this.displayMemory = displayMemory;
    }


    @Override
    public void connect(Plug plug) {
        this.plug = plug;

        if (displayMemory != null) {
            plug.onDisplayData(displayMemory);
        }
    }

    @Override
    public void disconnect() {
        plug = displayMemory -> {};
    }

    public void onFrame() {
        plug.onDisplayData(displayMemory);
    }
}
