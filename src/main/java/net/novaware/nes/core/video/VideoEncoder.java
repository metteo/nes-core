package net.novaware.nes.core.video;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;

/**
 * Accepts color indexed dots from ppu and assembles a raw RGB frame
 *
 * TODO: let this name / arch. sit and see if its OK
 */
@BoardScope
public class VideoEncoder implements ClockReceiver {

    @Inject
    public VideoEncoder() {

    }

    @Override
    public int cycle() {
        return 1;
    }


    // VE uses a method to write a pixel to back buffer but doesn't know which is it A or B.
    // when VBlank starts it calls swap method which also triggers the rest of the rendering pipeline for now the front buffer

    // TODO: there is a single pixel buffer which delays pixel output to display memory
}
