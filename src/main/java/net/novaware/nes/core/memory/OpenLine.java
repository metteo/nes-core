package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;

public class OpenLine implements DataBus.Line {

    private @Unsigned byte value = UBYTE_MAX_VALUE;

    @Override
    public @Unsigned byte data() {
        return value;
    }

    @Override
    public void data(@Unsigned byte data) {
        value = data;
    }
}
