package net.novaware.nes.core.ppu.unit;

import net.novaware.nes.core.register.ByteShifter;
import org.checkerframework.checker.signedness.qual.Unsigned;

// TODO: refactor this into a branchless impl, without using external refs like integer counter or ByteShifter
// TODO: consider data oriented. One instance having arrays for fields where every index is a separate unit
// TODO: consider packing multiple sprites bits into a byte. 2 sparse arrays of 256 bytes could contain a full line of 8 sprites
//  no shifting, no counters. just extract the first non 0 bits for muxing
//  having more than 2x or 4x amount of output units would require short/int, or additional output units, to be decided
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
