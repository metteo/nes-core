package net.novaware.nes.core.ppu.unit;

import net.novaware.nes.core.register.ByteShifter;
import org.checkerframework.checker.signedness.qual.Unsigned;

// TODO: refactor this into a branchless impl, without using external refs like integer counter or ByteShifter
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

    public int countDown; // [0, x] waiting

    public int xCounter;

    public boolean active;


    // FIXME: this method takes a lot of cpu time
    public void maybeShiftPlanes() {
        if (countDown > 0) {
            countDown--;
            return;
        }

        if (xCounter > 0) {
            xCounter--;
            shifter.shiftPlanes();
        }
    }

    public boolean shouldDraw() {
        return active & countDown == 0 && xCounter > 0;
    }
}
