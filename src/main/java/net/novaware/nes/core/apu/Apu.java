package net.novaware.nes.core.apu;

import jakarta.inject.Inject;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.clock.ClockReceiver;

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
