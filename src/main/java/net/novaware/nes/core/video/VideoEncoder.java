package net.novaware.nes.core.video;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.config.BorderRegion;
import net.novaware.nes.core.config.CoreConfig;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.port.internal.DisplayPortImpl;
import net.novaware.nes.core.ppu.memory.DisplayMemory;
import net.novaware.nes.core.ppu.register.VideoOutRegister;

/**
 * Accepts color indexed dots from ppu and assembles the frame
 */
@BoardScope
public class VideoEncoder implements ClockReceiver {

    private final VideoStandard videoStandard;
    private final BorderRegion borderRegion;

    private final VideoOutRegister videoOut;
    private final DisplayMemory displayMemory;

    private final DisplayPortImpl displayPort;

    @Inject
    public VideoEncoder(
        CoreConfig config,
        VideoOutRegister videoOut,
        DisplayMemory displayMemory,
        final DisplayPortImpl displayPort
    ) {
        this.videoStandard = config.getVideoStandard();
        this.borderRegion = BorderRegion.of(videoStandard);
        this.videoOut = videoOut;
        this.displayMemory = displayMemory;
        this.displayPort = displayPort;
    }

    @Override
    public int cycle() {
        int y = videoOut.getY();
        int x = videoOut.getX();

        int screenY = borderRegion.getTop() + y;
        int screenX = borderRegion.getLeft() + x;

        if (y < 0 && x < 0) {
            displayMemory.setColor(videoOut.getColorRef());
        } else {
            displayMemory.setColor(screenY, screenX, videoOut.getColorRef());
        }

        // TODO: figure out a better way to notify about swap
        if (y == videoStandard.getActiveHeight() - 1 && x == videoStandard.getActiveWidth() - 1) {
            displayMemory.swap();
            displayPort.onFrame();
        }

        return 1;
    }

    // TODO: there is a single pixel buffer which delays pixel output to display memory?
}
