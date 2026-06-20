package net.novaware.nes.core.io.memory;

import net.novaware.nes.core.memory.DataBus;
import net.novaware.nes.core.memory.MemoryDevice;
import net.novaware.nes.core.memory.OpenLine;
import net.novaware.nes.core.register.BooleanRegister;
import net.novaware.nes.core.util.Hex;
import net.novaware.nes.core.util.Nameable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.nes.core.util.Masks.BIT_0;
import static net.novaware.nes.core.util.UTypes.UBYTE_0;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class JoyStrobeDevice implements MemoryDevice.WriteOnly, Nameable {

    private final String name;

    private final @Unsigned short address;
    private final BooleanRegister strobeRegister;

    private DataBus.Line dataLine = new OpenLine();

    public JoyStrobeDevice(
            String name,
            @Unsigned short address,
            BooleanRegister strobeRegister // TODO: should be an InputDevice that reloads the joy regs
    ) {
        this.name = name;
        this.address = address;
        this.strobeRegister = strobeRegister;
    }

    @Override
    public @Unsigned short getStartAddress() {
        return address;
    }

    @Override
    public @Unsigned short getEndAddress() {
        return address;
    }

    @Override
    public void probe(@Unsigned short address, DataBus.Line dataLine) {
        assert this.address == address : "only direct access is supported";

        dataLine.data(strobeRegister.get() ? ubyte(1) : UBYTE_0);
    }

    @Override
    public void onAccess(@Unsigned short address) {
        assert this.address == address : "only direct access is supported";
    }

    @Override
    public void onWrite() {
        @Unsigned byte data = dataLine.data();

        boolean strobe = (sint(data) & BIT_0) == 1;

        strobeRegister.set(strobe);
    }

    @Override
    public void onAttach(DataBus.Line dataLine) {
        this.dataLine = dataLine;
    }

    @Override
    public void onDetach() {
        this.dataLine = new OpenLine();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (0x" + Hex.s(address) + "): " + (strobeRegister.get() ? 1 : 0);
    }
}
