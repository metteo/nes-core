package net.novaware.nes.core.video;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;
import net.novaware.nes.core.ppu.memory.DisplayMemory;
import net.novaware.nes.core.ppu.register.VideoOutRegister;

/**
 * Accepts color indexed dots from ppu and assembles a raw RGB frame
 *
 * TODO: let this name / arch. sit and see if its OK
 */
@BoardScope
public class VideoEncoder implements ClockReceiver {

    private final VideoOutRegister videoOut;
    private final DisplayMemory displayMemory;

    @Inject
    public VideoEncoder(
        VideoOutRegister videoOut,
        DisplayMemory displayMemory
    ) {
        this.videoOut = videoOut;
        this.displayMemory = displayMemory;
    }

    @Override
    public int cycle() {
        int y = videoOut.getY();
        int x = videoOut.getX();

        displayMemory.setColor(y, x, videoOut.getColorIndex());

        if (y == 239 && x == 255) {
            displayMemory.swap();
        }

        return 1;
    }


    // VE uses a method to write a pixel to back buffer but doesn't know which is it A or B.
    // when VBlank starts it calls swap method which also triggers the rest of the rendering pipeline for now the front buffer

    // TODO: there is a single pixel buffer which delays pixel output to display memory
}
