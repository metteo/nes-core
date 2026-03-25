package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Both ABL and ABH (Address Bus Low & High)
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bus_(computing)#Address_bus">Address Bus</a>
 */
public interface AddressBus {

    interface Line {
        ControlBus.Line access(@Unsigned short address);
    }

    interface Device {

        @Unsigned short getStartAddress();
        @Unsigned short getEndAddress();

        // one way so just value instead of line
        void onAccess(@Unsigned short address);
    }
}
