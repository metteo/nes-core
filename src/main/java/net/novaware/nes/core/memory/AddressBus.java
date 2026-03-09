package net.novaware.nes.core.memory;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Both ABL and ABH (Address Bus Low & High)
 * @param <T>
 */
public interface AddressBus<T> {

    /**
     * Specify memory location for read or write.
     * @param address
     *
     * @see <a href="https://en.wikipedia.org/wiki/Bus_(computing)#Address_bus">Address Bus</a>
     */
    void specify(final @Unsigned short address); // TODO: specify calls should propagate to every device on the bus?
    // Only the interested devices on the way will configure path for incoming (or not) read or write
    // Open bus behavior is usually hi byte of the address

    T specifyThen(@Unsigned short address);
}
