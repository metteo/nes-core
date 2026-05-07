package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.UBYTE_MAX_VALUE;

public class DataLine implements DataBus.Line {

    /**
     * @see <a href="https://www.nesdev.org/wiki/Open_bus_behavior">Open Bus Behavior on nesdev.org</a>
     */
    /*
     * Open bus can be limited to part of a byte. The controller ports ($4016, $4017) affect only bits 4-0.
     * Bits 7-5 repeat the corresponding bits from the previous read, usually 010 from the high byte $40.
     *
     * Part of Address Bus is shared with Data Bus so some leftover charge from address bits may be readable as data bits
     */
    private boolean openBus = true;
    // TODO: maybe track bus conflict (if 2+ devices write to the data line)

    private @Unsigned byte previous = UBYTE_MAX_VALUE;
    private @Unsigned byte current = UBYTE_MAX_VALUE;

    public boolean isOpenBus() {
        return openBus;
    }

    public @Unsigned byte cycle() {
        @Unsigned byte result = openBus ? previous : current;

        openBus = true;
        previous = current;
        current = UBYTE_MAX_VALUE;

        return result;
    }

    @Override
    public @Unsigned byte data() {
        return current;
    }

    @Override
    public void data(@Unsigned byte data) {
        openBus = false;
        this.current = data;
    }
}
