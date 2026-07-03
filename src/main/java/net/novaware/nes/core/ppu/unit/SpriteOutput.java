package net.novaware.nes.core.ppu.unit;

import net.novaware.nes.core.register.ByteShifter;
import net.novaware.nes.core.register.IntegerCounter;
import org.checkerframework.checker.signedness.qual.Unsigned;

public class SpriteOutput {

    enum State {
        WAITING,
        DRAWING,
        IDLE
    }

    // TODO: maybe have 2 shifters for cases when the sprite hangs off the right side and should wrap into left side
    // but only with horizontal mirroring like Mario Bros or Ice Climber, Wrecking Crew
    public ByteShifter shifter = new ByteShifter("SPOU?");

    public @Unsigned byte palette;

    public boolean hidden;

    public IntegerCounter countDown = new IntegerCounter("SPOU?cd"); // [0, x] waiting

    public IntegerCounter xCounter = new IntegerCounter("SPOU?x");

    public State state = State.IDLE;

    // FIXME: shifting or counting down or state change is wrong. sprites don't show up on first dot column!
    // FIXME: this method takes a lot of cpu time
    public void maybeShiftPlanes() {
        switch(state) {
            case WAITING -> {
                if (countDown.isPositive()) {
                    countDown.decrement();
                } else {
                    state = State.DRAWING;
                }
            }
            case DRAWING -> {
                if(xCounter.isPositive()) {
                    xCounter.decrement();
                    shifter.shiftPlanes();
                } else {
                    state = State.IDLE;
                }
            }
            case IDLE -> {}
        }
    }
}
