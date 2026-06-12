package net.novaware.nes.core.apu;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;

/**
 * Audio Processing Unit <br>
 * <br>
 * Part of 2A03 / 2A07 (NTSC / PAL) <br>
 * <br>
 * @see <a href="https://www.nesdev.org/wiki/APU">APU on nesdev.org</a>
 */
@BoardScope
public class Apu implements ClockReceiver {

    @Inject
    public Apu() {

    }

    public void initialize() {

    }

    @Override
    public int cycle() {
        return 1;
    }
}
