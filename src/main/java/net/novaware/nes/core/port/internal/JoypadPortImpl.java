package net.novaware.nes.core.port.internal;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.novaware.nes.core.board.inject.BoardScope;
import net.novaware.nes.core.port.JoypadPort;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.register.ByteRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.UTypes.UBYTE_0;

@BoardScope
public class JoypadPortImpl implements JoypadPort {

    // TODO: should use Pins instead of registers directly
    private final BooleanRegister joyStrobe;

    private final ByteRegister joy1Data;
    private final ByteRegister joy2Data;

    private Plug plug = () -> UBYTE_0;

    @Inject // TODO: temporary, there should be 2 of those provided by the module
    public JoypadPortImpl(
        @Named("JOY_STROBE") BooleanRegister joyStrobe,

        @Named("JOY1_DATA") ByteRegister joy1Data,
        @Named("JOY2_DATA") ByteRegister joy2Data
    ) {

        this.joyStrobe = joyStrobe;

        this.joy1Data = joy1Data;
        this.joy2Data = joy2Data;
    }

    @Override
    public void connect(Plug plug) {
        this.plug = plug;
    }

    @Override
    public void disconnect() {
        plug = () -> UBYTE_0;
    }

    public void onStrobeChange(boolean newStrobe) {
        final boolean oldStrobe = joyStrobe.get();
        joyStrobe.set(newStrobe);

        if (oldStrobe && !newStrobe) { // TODO: should be continuous, not once at the falling edge
            @Unsigned byte state = plug.getState();
            joy1Data.set(state);
        }
    }
}
