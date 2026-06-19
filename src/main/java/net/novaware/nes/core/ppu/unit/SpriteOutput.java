package net.novaware.nes.core.ppu.unit;

import net.novaware.nes.core.register.ByteShifter;
import net.novaware.nes.core.register.IntegerCounter;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.jspecify.annotations.Nullable;

public class SpriteOutput {

    private @Nullable ByteShifter shifter;

    private @Unsigned byte palette;

    private boolean hidden;

    private @Nullable IntegerCounter countDown; // [0, x] waiting, [-7, 0] rendering
}
